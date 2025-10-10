package com.mycollege.schedule.app.activity.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.mycollege.schedule.app.activity.ui.state.DataEvent
import com.mycollege.schedule.app.activity.ui.state.MainViewModel
import com.mycollege.schedule.app.activity.ui.state.StartViewModel
import com.mycollege.schedule.app.navigation.AddNavGraph
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.shared.ui.theme.background
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.ok.tracer.crash.report.TracerCrashReport
import ru.rustore.sdk.remoteconfig.RemoteConfigClient

@Stable
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // initializing all required viewModels on app startup
    private val mainViewModel: MainViewModel by viewModels()
    private val groupViewModel: GroupViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val startViewModel: StartViewModel by viewModels()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splash = installSplashScreen()

        splash.setKeepOnScreenCondition {
            true
        }

        lifecycleScope.launch {
            delay(1000)
            splash.setKeepOnScreenCondition { false }
        }

        groupViewModel.init()

        enableEdgeToEdge()
        setContent {

            Box(modifier = Modifier.Companion.fillMaxSize().background(background)) {

                val scope = rememberCoroutineScope()
                val navController = rememberNavController()

                // true - only once | does not start when recomposes
                LaunchedEffect(true) {

                    requestPermissionsIfNeeded()

                    scope.launch {
                        mainViewModel.handleEvent(DataEvent.RestoreCache)
                        mainViewModel.handleEvent(DataEvent.FetchData)
                        mainViewModel.handleEvent(DataEvent.SetupCacheUpdater)
                    }

                    RemoteConfigClient.Companion.instance
                        .getRemoteConfig().addOnSuccessListener { rc ->

                            try {

                                val server = rc.getString("ScheduleServer")
                                val accessToken = rc.getString("ScheduleServiceAccessToken")

                                mainViewModel.cacheManager.saveScheduleServerConfiguration(
                                    CacheManager.ScheduleServerConfiguration(server, "Bearer $accessToken")
                                )

                                Log.i("MainActivity", "Конфигурация сервера получена")

                            }
                            catch (e: Exception) {
                                TracerCrashReport.report(e, issueKey = "RUSTORE_REMOTE_CONFIG")
                                Log.e("RemoteConfigService", "Ошибка конфигурации: ${e.message}", e)
                            }
                        }
                        .addOnFailureListener { e ->
                            TracerCrashReport.report(e, issueKey = "RUSTORE_REMOTE_CONFIG")
                            Log.e("RuStoreMessagingService", "RemoteConfig fetch failed: ${e.message}", e)
                        }

                }

                // базовые настройки
                startViewModel.settingsInit()

                // hide system ui navigation panel
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                AddNavGraph(
                    navController = navController,
                    mainViewModel = mainViewModel,
                    startViewModel = startViewModel,
                    groupViewModel = groupViewModel,
                    scheduleViewModel = scheduleViewModel,
                    settingsViewModel = settingsViewModel
                )

            }
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:$packageName".toUri()
                }
                startActivity(intent)
            }
        }

        val pm = getSystemService(PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = "package:$packageName".toUri()
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.destroyNotifications()
    }

}