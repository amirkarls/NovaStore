package com.novastore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novastore.app.data.GitHubApi
import com.novastore.app.data.Preferences
import com.novastore.app.i18n.Strings
import com.novastore.app.ui.detail.DetailScreen
import com.novastore.app.ui.language.LanguageScreen
import com.novastore.app.ui.language.WarningScreen
import com.novastore.app.ui.main.MainScreen
import com.novastore.app.ui.settings.SettingsScreen
import com.novastore.app.ui.theme.NovaStoreTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val prefs = remember { Preferences(applicationContext) }
            var language by remember { mutableStateOf("ru") }
            var loaded by remember { mutableStateOf(false) }
            var languageSelected by remember { mutableStateOf(false) }
            var showWarning by remember { mutableStateOf(false) }
            var pendingLang by remember { mutableStateOf<String?>(null) }
            var dynamicColor by remember { mutableStateOf(true) }
            var accentIndex by remember { mutableStateOf(0) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                language = prefs.getLanguage()
                languageSelected = prefs.isLanguageSelected()
                dynamicColor = prefs.getDynamicColor()
                accentIndex = prefs.getAccentColor()
                GitHubApi.token = prefs.getGithubToken()
                loaded = true
            }

            NovaStoreTheme(
                dynamicColor = dynamicColor,
                accentIndex = accentIndex
            ) {
                when {
                    !loaded -> {}

                    !languageSelected -> {
                        LanguageScreen(onSelected = { lang ->
                            scope.launch {
                                if (Strings.needsWarning(lang)) {
                                    pendingLang = lang
                                    showWarning = true
                                } else {
                                    prefs.setLanguage(lang)
                                    prefs.setLanguageSelected()
                                    language = lang
                                    languageSelected = true
                                }
                            }
                        })
                    }

                    showWarning && pendingLang != null -> {
                        WarningScreen(
                            language = pendingLang!!,
                            onAccept = {
                                val lang = pendingLang!!
                                scope.launch {
                                    prefs.setLanguage(lang)
                                    prefs.setLanguageSelected()
                                    language = lang
                                    languageSelected = true
                                    showWarning = false
                                    pendingLang = null
                                }
                            }
                        )
                    }

                    else -> {
                        val navController = rememberNavController()
                        val items = listOf(
                            Triple("main", "tab_catalog", Icons.Filled.Apps),
                            Triple("settings", "tab_settings", Icons.Filled.Settings)
                        )

                        Scaffold(
                            bottomBar = {
                                NavigationBar {
                                    val backStack by navController.currentBackStackEntryAsState()
                                    val route = backStack?.destination?.route
                                    items.forEach { (r, labelKey, icon) ->
                                        NavigationBarItem(
                                            selected = route == r,
                                            onClick = {
                                                navController.navigate(r) {
                                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = { Icon(icon, null) },
                                            label = { Text(Strings.get(language, labelKey)) }
                                        )
                                    }
                                }
                            }
                        ) { padding ->
                            NavHost(
                                navController = navController,
                                startDestination = "main",
                                modifier = Modifier.padding(padding)
                            ) {
                                composable("main") {
                                    MainScreen(language = language) { pkg ->
                                        navController.navigate("detail/$pkg")
                                    }
                                }
                                composable("settings") {
                                    SettingsScreen(
                                        language = language,
                                        dynamicColor = dynamicColor,
                                        accentIndex = accentIndex,
                                        onLanguageChange = { lang ->
                                            scope.launch {
                                                if (Strings.needsWarning(lang)) {
                                                    pendingLang = lang
                                                    showWarning = true
                                                } else {
                                                    prefs.setLanguage(lang)
                                                    language = lang
                                                }
                                            }
                                        },
                                        onDynamicColorChange = { value ->
                                            scope.launch {
                                                prefs.setDynamicColor(value)
                                                dynamicColor = value
                                            }
                                        },
                                        onAccentColorChange = { index ->
                                            scope.launch {
                                                prefs.setAccentColor(index)
                                                accentIndex = index
                                            }
                                        }
                                    )
                                }
                                composable("detail/{pkg}") { back ->
                                    val pkg = back.arguments?.getString("pkg") ?: ""
                                    DetailScreen(
                                        packageName = pkg,
                                        language = language,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
