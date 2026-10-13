package com.mycollege.schedule.feature.surveys.data.repository

import androidx.compose.runtime.Stable
import com.google.gson.JsonParser
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.core.cache.CacheManager
import com.mycollege.schedule.core.network.RetrofitClient
import com.mycollege.schedule.core.network.api.quizzes.QuizzesApi
import com.mycollege.schedule.core.network.dto.quizzes.Quiz
import com.mycollege.schedule.core.network.dto.quizzes.QuizSubmitRequest
import com.mycollege.schedule.core.network.dto.quizzes.QuizSubmitResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ошибка API опросов с текстом, пригодным для показа пользователю
 */
class SurveyApiException(
    val code: Int?,
    override val message: String
) : Exception(message)

/**
 * Доступ к API опросов (api/v1/quizzes). Base URL и токен — из конфигурации сервера (RemoteConfig),
 * в debug-сборке их можно переопределить на локальный стенд (см. app/build.gradle.kts).
 */
@Stable
@Singleton
class SurveyRepository @Inject constructor(
    private val cacheManager: CacheManager
) {

    private data class Endpoint(val baseUrl: String, val accessToken: String?)

    /**
     * Есть ли куда ходить: на холодном старте RemoteConfig может ещё не прийти
     */
    fun isConfigured(): Boolean = endpoint() != null

    private fun endpoint(): Endpoint? {
        val server = cacheManager.loadScheduleServerConfiguration()

        val debugUrl = BuildConfig.SURVEYS_DEBUG_BASE_URL
        val baseUrl = debugUrl.ifBlank { server?.serverUrl.orEmpty() }
        if (baseUrl.isBlank()) return null

        val debugToken = BuildConfig.SURVEYS_DEBUG_TOKEN
        val token = when {
            debugToken.isNotBlank() ->
                if (debugToken.startsWith("Bearer ")) debugToken else "Bearer $debugToken"
            else -> server?.accessToken?.takeIf { it.isNotBlank() && it != "Bearer " }
        }

        return Endpoint(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/", token)
    }

    private fun api(): Pair<QuizzesApi, String?> {
        val endpoint = endpoint() ?: throw SurveyApiException(null, "Нет связи с сервером. Попробуйте позже")
        return RetrofitClient(endpoint.baseUrl).quizzesApi to endpoint.accessToken
    }

    suspend fun search(group: String?, respondent: String): List<Quiz> = call {
        val (api, token) = api()
        api.search(token, group, respondent).quizzes.orEmpty()
    }

    suspend fun quiz(id: Long): Quiz = call {
        val (api, token) = api()
        api.quiz(token, id)
    }

    suspend fun submit(request: QuizSubmitRequest): QuizSubmitResponse = call {
        val (api, token) = api()
        api.submit(token, request)
    }

    private suspend fun <T> call(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: SurveyApiException) {
            throw e
        } catch (e: HttpException) {
            throw SurveyApiException(e.code(), errorText(e))
        } catch (e: IOException) {
            throw SurveyApiException(null, "Нет связи с сервером. Попробуйте позже")
        } catch (e: Exception) {
            throw SurveyApiException(null, "Что-то пошло не так. Попробуйте позже")
        }
    }

    /**
     * Текст из тела 400/409: сервер отдаёт либо строку, либо JSON с полем message/error/detail
     */
    private fun errorText(e: HttpException): String {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()?.trim().orEmpty()

        val fromJson = runCatching {
            val element = JsonParser.parseString(body)
            when {
                element.isJsonPrimitive -> element.asString
                element.isJsonObject -> {
                    val obj = element.asJsonObject
                    listOf("message", "error", "detail", "title")
                        .firstNotNullOfOrNull { key ->
                            obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }
                        }
                }
                else -> null
            }
        }.getOrNull()

        val text = fromJson ?: body.takeIf { it.isNotBlank() && !it.startsWith("<") && !it.startsWith("{") }
        return text ?: when (e.code()) {
            409 -> "Вы уже прошли этот опрос"
            400 -> "Проверьте ответы и попробуйте ещё раз"
            else -> "Сервер не ответил (${e.code()}). Попробуйте позже"
        }
    }

}
