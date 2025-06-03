package com.mycollege.schedule.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.app.activity.domain.usecases.GetScheduleUseCase
import com.mycollege.schedule.app.workmanager.GroupSyncWorker
import com.mycollege.schedule.app.workmanager.ScheduleWorker
import com.mycollege.schedule.app.workmanager.WeekChangeWorker
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.network.remote.RemoteConfigListener
import com.mycollege.schedule.feature.schedule.domain.usecase.GetChosenGroupUseCase
import com.mycollege.schedule.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp
import ru.ok.tracer.HasTracerConfiguration
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration
import ru.ok.tracer.crash.report.TracerCrashReport
import ru.ok.tracer.disk.usage.DiskUsageConfiguration
import ru.ok.tracer.heap.dumps.HeapDumpConfiguration
import ru.rustore.sdk.appupdate.manager.factory.RuStoreAppUpdateManagerFactory
import ru.rustore.sdk.appupdate.model.AppUpdateOptions
import ru.rustore.sdk.appupdate.model.UpdateAvailability
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult
import ru.rustore.sdk.pushclient.RuStorePushClient
import ru.rustore.sdk.pushclient.common.logger.DefaultLogger
import ru.rustore.sdk.remoteconfig.AppId
import ru.rustore.sdk.remoteconfig.AppVersion
import ru.rustore.sdk.remoteconfig.DeviceId
import ru.rustore.sdk.remoteconfig.RemoteConfigClientBuilder
import ru.rustore.sdk.remoteconfig.UpdateBehaviour
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), HasTracerConfiguration, Configuration.Provider {

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

        val updateManager = RuStoreAppUpdateManagerFactory.create(applicationContext)

        updateManager.getAppUpdateInfo().addOnSuccessListener { appUpdateInfo ->
            Log.d("App", "check update")
            if (appUpdateInfo.updateAvailability == UpdateAvailability.UPDATE_AVAILABLE) {
                updateManager.startUpdateFlow(appUpdateInfo, AppUpdateOptions.Builder().build()).addOnSuccessListener { resultCode ->
                    if (resultCode == Activity.RESULT_CANCELED) {
                        // Пользователь отказался от скачивания
                    }
                }
                .addOnFailureListener { throwable ->
                    Log.e("App", "startUpdateFlow error", throwable)
                }
            }
            else {
                Log.d("App", "check update result: ${appUpdateInfo.updateAvailability}")
            }
        }
        .addOnFailureListener { throwable ->
            Log.e("App", "getAppUpdateInfo error", throwable)
        }

        MobileAds.initialize(this) {
            Log.i("MobileAds", "Initialized")
        }

        val listener = RemoteConfigListener()

        RemoteConfigClientBuilder(
            appId = AppId(BuildConfig.REMOTE_CONFIG_APP_ID),
            context = applicationContext
        ).setDeviceId(
            DeviceId(
                Settings.Secure.getString(
                    applicationContext.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            )
        )
            .setAppVersion(AppVersion(BuildConfig.VERSION_NAME))
            .setUpdateBehaviour(UpdateBehaviour.Actual)
            .setRemoteConfigClientEventListener(listener)
            .build()
            .init()

        RuStorePushClient.init(
            application = this,
            projectId = BuildConfig.PUSH_CLIENT_PROJECT_ID,
            logger = DefaultLogger()
        )

        RuStorePushClient.checkPushAvailability()
            .addOnSuccessListener { result ->
                if (result is FeatureAvailabilityResult.Available) {

                    RuStorePushClient.getToken()
                        .addOnSuccessListener { resultToken ->
                            Log.d("App", "getToken onSuccess token = $resultToken")
                        }
                        .addOnFailureListener { throwable ->
                            Log.e("App", "getToken onFailure", throwable)
                            TracerCrashReport.report(throwable, issueKey = "RUSTORE_PUSH_CLIENT")
                        }

                }
            }

    }

    override val tracerConfiguration: List<TracerConfiguration>
        get() = listOf(
            CrashReportConfiguration.Companion.build {
                setEnabled(true)
                setSendAnr(true)
                setNativeEnabled(true)
            },
            CrashFreeConfiguration.Companion.build {
                setEnabled(true)
            },
            HeapDumpConfiguration.Companion.build {
                setEnabled(true)
            },
            DiskUsageConfiguration.Companion.build {
                setEnabled(true)
                setProbability(1)
            },
        )

}

class CustomWorkerFactory @Inject constructor(
    private val cacheManager: CacheManager,
    private var getScheduleUseCase: GetScheduleUseCase,
    private val getChosenGroupUseCase: GetChosenGroupUseCase,
    private val getTodayScheduleUseCase: GetTodayScheduleUseCase
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            GroupSyncWorker::class.java.name -> GroupSyncWorker(appContext, workerParameters, cacheManager, getScheduleUseCase)
            ScheduleWorker::class.java.name -> ScheduleWorker(appContext, workerParameters, cacheManager, getChosenGroupUseCase, getTodayScheduleUseCase)
            WeekChangeWorker::class.java.name -> WeekChangeWorker(appContext, workerParameters, cacheManager)
            else -> null
        }
    }
}