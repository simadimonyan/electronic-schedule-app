package com.mycollege.schedule.presentation.screens.start

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.data.cache.CacheManager
import com.mycollege.schedule.data.network.RetrofitClient
import com.mycollege.schedule.data.network.dto.PushTokenRequest
import com.mycollege.schedule.presentation.screens.start.data.DataEvent
import com.mycollege.schedule.presentation.navigation.AddNavGraph
import com.mycollege.schedule.presentation.screens.groups.data.GroupsViewModel
import com.mycollege.schedule.presentation.screens.schedule.data.ScheduleViewModel
import com.mycollege.schedule.presentation.ui.theme.background
import com.mycollege.schedule.presentation.screens.start.data.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.ok.tracer.crash.report.TracerCrashReport
import ru.rustore.sdk.remoteconfig.RemoteConfigClient

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // initializing all required viewModels on app startup
    private val mainViewModel: MainViewModel by viewModels()
    private val groupsViewModel: GroupsViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        groupsViewModel.init()
        scheduleViewModel.init()

        enableEdgeToEdge()
        setContent {
            Box(modifier = Modifier.fillMaxSize().background(background)) {

                val scope = rememberCoroutineScope()
                val navController = rememberNavController()

                // true - only once | does not start when recomposes
                LaunchedEffect(true) {
                    scope.launch {
                        mainViewModel.handleEvent(DataEvent.RestoreCache)
                        mainViewModel.handleEvent(DataEvent.FetchData)
                        mainViewModel.handleEvent(DataEvent.SetupCacheUpdater)
                    }

                    RemoteConfigClient.instance
                        .getRemoteConfig().addOnSuccessListener { rc ->

                            // IP address
                            val server = rc.getString("PushServer")
                            val pushApproval = rc.getBoolean("PushApproval")
                            val accessToken = BuildConfig.ACCESS_TOKEN

                            if (server.isNotEmpty() && pushApproval) {

                                scope.launch {
                                    try {
                                        val config = mainViewModel.cacheManager.loadLastRuStoreConfig()

                                        if (config != null && !config.sentToServer) {

                                            Log.d("RuStoreMessagingService", "Отправка запроса")

                                            try {
                                                val response = RetrofitClient(server).ledgerApi.pullTokenUp(
                                                    PushTokenRequest(
                                                        Settings.Secure.getString(applicationContext.contentResolver, Settings.Secure.ANDROID_ID),
                                                        Build.MODEL,
                                                        config.pushToken,
                                                        accessToken
                                                    )
                                                )

                                                Log.d("RuStoreMessagingService", "Ответ сервера: ${response}")

                                            } catch (e: Exception) {
                                                Log.w("RuStoreMessagingService", "Ошибка парсинга JSON: ${e.message}")
                                            }

                                            // token has sent
                                            mainViewModel.cacheManager.saveActualRuStoreConfig(CacheManager.RuStoreConfig(config.pushToken, true))

                                        }
                                    } catch (e: Exception) {
                                        Log.e("RuStoreMessagingService", "Ошибка запроса: ${e.message}", e)
                                    }
                                }

                            }
                        }
                        .addOnFailureListener { e ->
                            TracerCrashReport.report(e, issueKey = "RUSTORE_REMOTE_CONFIG")
                            Log.e("RuStoreMessagingService", "RemoteConfig fetch failed: ${e.message}", e)
                        }

                }

                // hide system ui navigation panel
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowInsetsControllerCompat(window, window.decorView)
                insetsController.hide(WindowInsetsCompat.Type.navigationBars())
                insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                AddNavGraph(
                    navController = navController,
                    mainViewModel = mainViewModel,
                    groupsViewModel = groupsViewModel,
                    scheduleViewModel = scheduleViewModel
                )

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.destroyNotifications()
    }

}


