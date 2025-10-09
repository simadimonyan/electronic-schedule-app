package com.mycollege.schedule.core.network.api.teachers

import com.mycollege.schedule.core.network.dto.teachers.Teachers
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TeachersApi {

    @GET("api/v1/teachers/search")
    suspend fun search(@Header("Authorization") accessToken: String, @Query("department") department: String): Teachers

    @GET("api/v1/teachers/search")
    suspend fun search(@Header("Authorization") accessToken: String): Teachers

}