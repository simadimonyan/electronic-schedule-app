package com.mycollege.schedule.core.network.dto

data class PushTokenRequest(
    private val os: String,
    private val phoneModel: String,
    private val pushToken: String,
    private val accessToken: String,
)