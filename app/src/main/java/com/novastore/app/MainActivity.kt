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
import com.novastore.app.ui.detail.DetailScreen
import com.novastore.app.ui.main.MainScreen
import com.novastore.app.ui.settings.SettingsScreen
import com.novastore.app.ui.theme.NovaStoreTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaStoreTheme {
                val scope = rememberCoroutineScope()
                val prefs = remember { Preferences(applicationContext) }

                LaunchedEffect(Unit) {
                    val token = prefs.getGithubToken()
                    GitHubApi.token = token
                }

                val navController = rememberNavController()
                val items = listOf(
                    "main" to Icons.Filled.Apps,
                    "settings" to Icons.Filled.Settings
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val backStack by navController.currentBackStackEntryAsState()
                            val route = backStack?.destination?.route
                            items.forEach { (r, icon) ->
                                NavigationBarItem(
                                    selected = route == r,
                                    onClick = {
                                        navController.navigate(r) {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(icon, null) }
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
                            MainScreen(onAppClick = { pkg ->
                                navController.navigate("detail/$pkg")
                            })
                        }
                        composable("settings") { SettingsScreen() }
                        composable("detail/{pkg}") { back ->
                            val pkg = back.arguments?.getString("pkg") ?: ""
                            DetailScreen(packageName = pkg, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
