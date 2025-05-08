package com.mycollege.schedule.shared.resources

import android.content.Context
import androidx.compose.runtime.Immutable
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class ResourceManager @Inject constructor(
    private val context: Context
) {
    fun getString(resId: Int): String = context.getString(resId)

    fun getContext(): Context {
        return context
    }
}