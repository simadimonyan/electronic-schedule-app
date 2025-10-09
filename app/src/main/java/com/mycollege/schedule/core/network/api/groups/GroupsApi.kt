package com.mycollege.schedule.core.network.api.groups

import com.mycollege.schedule.core.network.dto.groups.Courses
import com.mycollege.schedule.core.network.dto.groups.Groups
import com.mycollege.schedule.core.network.dto.groups.Levels
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface GroupsApi {

    @GET("api/v1/groups/search")
    suspend fun search(@Header("Authorization") accessToken: String, @Query("course") course: Int): Groups

    @GET("api/v1/groups/search")
    suspend fun search(@Header("Authorization") accessToken: String, @Query("course") course: Int, @Query("level") level: String): Groups

    @GET("api/v1/groups/levels")
    suspend fun levels(@Header("Authorization") accessToken: String, @Query("course") course: Int): Levels

    @GET("api/v1/groups/courses")
    suspend fun courses(@Header("Authorization") accessToken: String): Courses

}