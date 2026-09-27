package com.novastore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novastore.app.ui.detail.DetailScreen
import com.novastore.app.ui.main.MainScreen
import com.novastore.app.ui.theme.NovaStoreTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovaStoreTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        MainScreen(onAppClick = { pkg ->
                            navController.navigate("detail/$pkg")
                        })
                    }
                    composable("detail/{pkg}") { backStackEntry ->
                        val pkg = backStackEntry.arguments?.getString("pkg") ?: ""
                        DetailScreen(packageName = pkg, onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
