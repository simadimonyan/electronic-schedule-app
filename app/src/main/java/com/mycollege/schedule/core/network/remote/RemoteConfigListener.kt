package com.mycollege.schedule.core.network.remote

import android.util.Log
import ru.ok.tracer.crash.report.TracerCrashReport
import ru.rustore.sdk.remoteconfig.RemoteConfigClientEventListener
import ru.rustore.sdk.remoteconfig.RemoteConfigException

class RemoteConfigListener : RemoteConfigClientEventListener {

    override fun backgroundJobErrors(exception: RemoteConfigException.BackgroundConfigUpdateError) {
        Log.e("RemoteConfig", "errorOccurred: $exception")
        TracerCrashReport.report(exception, issueKey = "RUSTORE_REMOTE_CONFIG")
    }

    override fun firstLoadComplete() {
        Log.d("RemoteConfig", "firstLoadComplete")
    }
    override fun initComplete() {
        Log.d("RemoteConfig", "initComplete")
    }
    override fun memoryCacheUpdated() {
        Log.d("RemoteConfig", "memoryCacheUpdated")
    }
    override fun persistentStorageUpdated() {
        Log.d("RemoteConfig", "persistentStorageUpdated")
    }
    override fun remoteConfigNetworkRequestFailure(throwable: Throwable) {
        Log.d("RemoteConfig", "remoteConfigNetworkRequestFailure")
    }

}