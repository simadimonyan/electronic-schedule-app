package com.mycollege.schedule.core.network.api.configs

import com.mycollege.schedule.core.network.dto.configs.WeekParityConfig
import retrofit2.http.GET
import retrofit2.http.Header

interface ConfigsApi {

    @GET("api/v1/configuration/week")
    suspend fun getWeek(@Header("Authorization") accessToken: String): WeekParityConfig

}