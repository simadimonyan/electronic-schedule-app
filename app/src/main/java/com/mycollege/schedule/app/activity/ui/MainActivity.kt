package com.mycollege.schedule.app.activity.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.ui.state.DataEvent
import com.mycollege.schedule.app.activity.ui.state.MainViewModel
import com.mycollege.schedule.app.activity.ui.state.StartViewModel
import com.mycollege.schedule.app.navigation.AddNavGraph
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.feature.groups.ui.state.GroupViewModel
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleViewModel
import com.mycollege.schedule.feature.settings.ui.state.SettingsViewModel
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.ScheduleTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.backgroundDark
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

    @SuppressLint("HardwareIds", "StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        groupViewModel.init()
        val darkTheme = groupViewModel.appStateHolder.appState.value.darkTheme

        setTheme(if (darkTheme) R.style.Theme_Schedule_Dark else R.style.Theme_Schedule_Light)

        val splash = installSplashScreen()

        splash.setKeepOnScreenCondition {
            true
        }

        lifecycleScope.launch {
            delay(1000)

            splash.setKeepOnScreenCondition { false }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {

            val appState by mainViewModel.appStateHolder.appState.collectAsState()
            val darkTheme = appState.darkTheme

            ScheduleTheme(darkTheme = darkTheme) {

                val originalDensity = LocalDensity.current
                CompositionLocalProvider(
                    LocalAppDarkTheme provides darkTheme,
                    LocalDensity provides Density(
                        density = originalDensity.density,
                        fontScale = 1.0f // Фиксированный масштаб шрифта
                    )
                ) {

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

                                        // базовые настройки (дублирование для запроса недели после загрузки)
                                        // не вызывается при отсутствии интернета (исключение происходит раньше)
                                        startViewModel.settingsInit()

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

                        // базовые настройки (дублирование для обработки отсутствия интернета)
                        startViewModel.settingsInit()

                        // hide system ui navigation panel
                        val darkMode = LocalAppDarkTheme.current
                        WindowCompat.setDecorFitsSystemWindows(window, false)
                        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                        //insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                        //insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                        window.isNavigationBarContrastEnforced = false

                        insetsController.isAppearanceLightStatusBars = !darkMode // dark icons color - true
                        insetsController.isAppearanceLightNavigationBars = !darkMode

                        AddNavGraph(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            startViewModel = startViewModel,
                            groupViewModel = groupViewModel,
                            scheduleViewModel = scheduleViewModel,
                            settingsViewModel = settingsViewModel
                        )

//                        val navPadding = WindowInsets.navigationBars.asPaddingValues()
//                        val navHeight = navPadding.calculateBottomPadding()
//
//                        if (navHeight > 0.dp) {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(navHeight)
//                                    .background(if (darkMode) backgroundDark else background)
//                                    .align(Alignment.BottomCenter)
//                            )
//                        }

                    }

                }

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