package com.novastore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.Crossfade
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novastore.app.data.GitHubApi
import com.novastore.app.util.NetworkUtils
import com.novastore.app.data.Preferences
import com.novastore.app.i18n.Strings
import com.novastore.app.ui.detail.DetailScreen
import com.novastore.app.ui.downloads.DownloadsScreen
import com.novastore.app.ui.language.LanguageScreen
import com.novastore.app.ui.language.WarningScreen
import com.novastore.app.ui.main.MainScreen
import com.novastore.app.ui.network.NoInternetScreen
import com.novastore.app.ui.components.CustomBottomNav
import com.novastore.app.ui.components.NavItem
import com.novastore.app.ui.settings.SettingsScreen
import com.novastore.app.ui.splash.SplashScreen
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
            var smoothAnimations by remember { mutableStateOf(true) }
            var updateNotifications by remember { mutableStateOf(true) }
            var online by remember { mutableStateOf(NetworkUtils.isOnline(applicationContext)) }
            var showSplash by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                language = prefs.getLanguage()
                languageSelected = prefs.isLanguageSelected()
                dynamicColor = prefs.getDynamicColor()
                accentIndex = prefs.getAccentColor()
                smoothAnimations = prefs.getSmoothAnimations()
                updateNotifications = prefs.getUpdateNotifications()
                GitHubApi.token = prefs.getGithubToken()
                loaded = true
            }

            NovaStoreTheme(dynamicColor = dynamicColor, accentIndex = accentIndex) {
                when {
                    showSplash -> {
                        SplashScreen(onFinished = { showSplash = false })
                    }
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
                        WarningScreen(language = pendingLang!!, onAccept = {
                            val lang = pendingLang!!
                            scope.launch {
                                prefs.setLanguage(lang)
                                prefs.setLanguageSelected()
                                language = lang
                                languageSelected = true
                                showWarning = false
                                pendingLang = null
                            }
                        })
                    }
                    !online -> {
                        NoInternetScreen(onRetry = {
                            online = NetworkUtils.isOnline(applicationContext)
                        })
                    }
                    else -> {
                        val navController = rememberNavController()
                        val items = listOf(
                            Triple("main", "tab_catalog", Icons.Filled.Apps),
                            Triple("downloads", "tab_downloads", Icons.Filled.Download),
                            Triple("settings", "tab_settings", Icons.Filled.Settings)
                        )
                        val backStack by navController.currentBackStackEntryAsState()
                        val currentRoute = backStack?.destination?.route

                        Scaffold(
                            bottomBar = {
                                CustomBottomNav(
                                    items = listOf(
                                        NavItem("main", Strings.get(language, "tab_catalog"), Icons.Filled.Apps),
                                        NavItem("downloads", Strings.get(language, "tab_downloads"), Icons.Filled.Download),
                                        NavItem("settings", Strings.get(language, "tab_settings"), Icons.Filled.Settings)
                                    ),
                                    selectedRoute = currentRoute,
                                    onItemClick = { r ->
                                        navController.navigate(r) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        ) { padding ->
                            NavHost(
                                navController = navController,
                                startDestination = "main",
                                modifier = Modifier.padding(padding)
                            ) {
                                composable("main") {
                                    MainScreen(
                                        language = language,
                                        updateNotifications = updateNotifications,
                                        onAppClick = { pkg -> navController.navigate("detail/$pkg") }
                                    )
                                }
                                composable("downloads") {
                                    DownloadsScreen(language = language)
                                }
                                composable("settings") {
                                    SettingsScreen(
                                        language = language,
                                        dynamicColor = dynamicColor,
                                        accentIndex = accentIndex,
                                        smoothAnimations = smoothAnimations,
                                        updateNotifications = updateNotifications,
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
                                        onDynamicColorChange = { v -> scope.launch { prefs.setDynamicColor(v); dynamicColor = v } },
                                        onAccentColorChange = { i -> scope.launch { prefs.setAccentColor(i); accentIndex = i } },
                                        onSmoothAnimationsChange = { v -> scope.launch { prefs.setSmoothAnimations(v); smoothAnimations = v } },
                                        onUpdateNotificationsChange = { v -> scope.launch { prefs.setUpdateNotifications(v); updateNotifications = v } }
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
