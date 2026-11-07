package com.ainotif.assistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ainotif.assistant.ui.log.LogCollector
import com.ainotif.assistant.ui.theme.AiNotifTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiNotifTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    NotificationApp()
                }
            }
        }
    }
}

enum class Screen {
    Setup, Inbox, DebugLog
}

@Composable
fun NotificationApp() {
    val navController = rememberNavController()
    val setupViewModel: SetupViewModel by hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Setup.name) {
        composable(Screen.Setup.name) {
            SetupScreen(
                onGranted = { navController.navigate(Screen.Inbox.name) { popUpTo(Screen.Setup.name) { inclusive = true } } },
                viewModel = setupViewModel
            )
        }
        composable(Screen.Inbox.name) {
            InboxScreen()
        }
        composable(Screen.DebugLog.name) {
            DebugLogScreen()
        }
    }
}
