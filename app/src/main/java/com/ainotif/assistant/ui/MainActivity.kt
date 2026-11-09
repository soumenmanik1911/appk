package com.ainotif.assistant.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ainotif.assistant.database.NotifEntity
import com.ainotif.assistant.ui.log.LogCollector
import com.ainotif.assistant.ui.theme.AiNotifTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val postNotificationsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            LogCollector.info("POST_NOTIFICATIONS permission granted")
        } else {
            LogCollector.error("POST_NOTIFICATIONS permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                postNotificationsLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val intent = intent
        if (intent.action == "OPEN_REMINDER") {
            val notifId = intent.getIntExtra("notifId", -1)
            if (notifId != -1) {
                val app = applicationContext as MyApplication
                androidx.work.WorkManager.getInstance(app)
                    .cancelUniqueWork("reminder_timeout_$notifId")
                app.repository.markOpened(notifId, opened = true)
                LogCollector.info("Reminder opened: notifId=$notifId openedAfter=true")
            }
        }

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
    Setup, Inbox, DebugLog, Snooze, Insights
}

@Composable
fun NotificationApp() {
    val navController = rememberNavController()
    val setupViewModel: SetupViewModel by hiltViewModel()

    NavHost(navController = navController, startDestination = Screen.Setup.name) {
        composable(Screen.Setup.name) {
            SetupScreen(
                onGranted = { navController.navigate(Screen.Inbox.name) { popUpTo(Screen.Setup.name) { inclusive = true } } },
                onInsights = { navController.navigate(Screen.Insights.name) },
                viewModel = setupViewModel
            )
        }
        composable(Screen.Inbox.name) {
            InboxScreen()
        }
        composable(Screen.DebugLog.name) {
            DebugLogScreen()
        }
        composable(Screen.Snooze.name) {
            val navBackStackEntry = navController.currentBackStackEntryAsState().value
            val notifId: Int = navBackStackEntry.arguments?.getInt("notifId") ?: 0
            val app = LocalContext.current.applicationContext as MyApplication
            SnoozeScreen(
                notifId = notifId,
                repository = app.repository,
                scheduler = AlarmScheduler(LocalContext.current, app.repository),
                onDismiss = { navController.popBackStack() }
            )
        }
        composable(Screen.Insights.name) {
            val app = LocalContext.current.applicationContext as MyApplication
            InsightsScreen(
                statsRepository = com.ainotif.assistant.stats.StatsRepository(app.repository)
            )
        }
    }
}

import androidx.compose.foundation.LocalContext
