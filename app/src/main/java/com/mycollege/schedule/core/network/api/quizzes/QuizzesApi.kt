package com.mycollege.schedule.core.network.api.quizzes

import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.core.network.dto.quizzes.QuizSubmitRequest
import com.mycollege.schedule.core.network.dto.quizzes.QuizSubmitResponse
import com.mycollege.schedule.core.network.dto.quizzes.Quizzes
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuizzesApi {

    /**
     * Открытые опросы для всех и для группы (group = null — только общие), без пройденных respondent
     */
    @GET("api/v1/quizzes/search")
    suspend fun search(
        @Header("Authorization") accessToken: String?,
        @Query("group") group: String?,
        @Query("respondent") respondent: String
    ): Quizzes

    @GET("api/v1/quizzes/{id}")
    suspend fun quiz(@Header("Authorization") accessToken: String?, @Path("id") id: Long): Quiz

    /**
     * 409 — «Вы уже прошли этот опрос», 400 — ошибка в анкете (текст в теле)
     */
    @POST("api/v1/quizzes/submit")
    suspend fun submit(@Header("Authorization") accessToken: String?, @Body request: QuizSubmitRequest): QuizSubmitResponse

}
