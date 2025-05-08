package com.mycollege.schedule.core.network

import androidx.compose.runtime.Immutable
import com.google.gson.GsonBuilder
import com.mycollege.schedule.core.network.api.LedgerAPI
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

//    private val logging = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    private val client = OkHttpClient.Builder()
//        .addInterceptor(logging)
//        .build()

    private val gson = GsonBuilder()
        .setLenient()  // "bad" json process
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(urlString)
           // .client(client) // --debug only
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val ledgerApi: LedgerAPI by lazy {
        retrofit.create(LedgerAPI::class.java)
    }

}
