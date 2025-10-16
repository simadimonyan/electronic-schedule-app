package com.mycollege.schedule.core.analitics

import com.my.tracker.MyTracker
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Tracker @Inject constructor() {

    private val id: String = generateUserId()

    // configuration settings
    fun init() {
        val trackerParams = MyTracker.getTrackerParams()
        val trackerConfig = MyTracker.getTrackerConfig()

        //MyTracker.setDebugMode(true)

        trackerParams.customUserId = id
    }

    private fun generateUserId(): String {
        return UUID.randomUUID().toString()
    }

}