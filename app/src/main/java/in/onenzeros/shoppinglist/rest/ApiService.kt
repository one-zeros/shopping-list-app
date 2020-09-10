package `in`.onenzeros.shoppinglist.rest

import `in`.onenzeros.shoppinglist.BuildConfig
import `in`.onenzeros.shoppinglist.model.DefaultListResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ApiService {

    companion object {
        fun create(): ApiService {
            val client
                    = OkHttpClient().newBuilder()
                        .addInterceptor(HttpLoggingInterceptor().apply {
                            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                        }).build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(
                    GsonConverterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.quickshoppinglist.com/")
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }

    @GET("https://api.quickshoppinglist.com/")
    fun getDefaultList(): Call<DefaultListResponse>

}