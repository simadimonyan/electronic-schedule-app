package com.mycollege.schedule.core.network

import androidx.compose.runtime.Immutable
import com.google.gson.GsonBuilder
import com.mycollege.schedule.core.network.api.configs.ConfigsApi
import com.mycollege.schedule.core.network.api.groups.GroupsApi
import com.mycollege.schedule.core.network.api.teachers.TeachersApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Immutable
class Network {

    companion object {

        fun connect(url: String, timeout: Int): Document {
            return Jsoup.connect(url)
                .userAgent("Mozilla")
                .timeout(timeout)
                .get()
        }

    }

}

@Immutable
class RetrofitClient(private val urlString: String) {

    private val logging = HttpLoggingInterceptor().apply { // --debug only
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder() // --debug only
        .addInterceptor(logging)
        .build()

    private val gson = GsonBuilder()
        .setLenient()  // "bad" json process
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(urlString)
            .client(client) // --debug only
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val configsApi: ConfigsApi by lazy {
        retrofit.create(ConfigsApi::class.java)
    }

    val groupsApi: GroupsApi by lazy {
        retrofit.create(GroupsApi::class.java)
    }

    val teachersApi: TeachersApi by lazy {
        retrofit.create(TeachersApi::class.java)
    }

}
