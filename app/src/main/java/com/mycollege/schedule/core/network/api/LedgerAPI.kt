package com.mycollege.schedule.core.network.api

import com.mycollege.schedule.core.network.dto.PushTokenRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface LedgerAPI {
    @POST("/ledger/pullTokenUp")
    @Headers("Content-Type: application/json")
    suspend fun pullTokenUp(@Body request: PushTokenRequest): String
}