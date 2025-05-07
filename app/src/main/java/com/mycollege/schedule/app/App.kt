package com.mycollege.schedule.app

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import android.util.Log
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.core.network.remote.RemoteConfigListener
import com.yandex.mobile.ads.common.MobileAds
import dagger.hilt.android.HiltAndroidApp
import ru.ok.tracer.HasTracerConfiguration
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration
import ru.ok.tracer.crash.report.TracerCrashReport
import ru.ok.tracer.disk.usage.DiskUsageConfiguration
import ru.ok.tracer.heap.dumps.HeapDumpConfiguration
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult
import ru.rustore.sdk.pushclient.RuStorePushClient
import ru.rustore.sdk.pushclient.common.logger.DefaultLogger
import ru.rustore.sdk.remoteconfig.AppId
import ru.rustore.sdk.remoteconfig.AppVersion
import ru.rustore.sdk.remoteconfig.DeviceId
import ru.rustore.sdk.remoteconfig.RemoteConfigClientBuilder
import ru.rustore.sdk.remoteconfig.UpdateBehaviour

@HiltAndroidApp
class App : Application(), HasTracerConfiguration {

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()

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