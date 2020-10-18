package `in`.onenzeros.shoppinglist.rest

import `in`.onenzeros.shoppinglist.BuildConfig
import `in`.onenzeros.shoppinglist.data.model.DefaultListResponse
import `in`.onenzeros.shoppinglist.data.model.SuggestionListResponse
import `in`.onenzeros.shoppinglist.rest.request.UpdateListRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

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

    @GET(".")
    fun getDefaultList(@Query("clientId") clientId: String): Call<DefaultListResponse>

    @GET("/{id}")
    fun getExistingList(@Path("id") id: String, @Query("clientId") clientId: String): Call<DefaultListResponse>

    @PUT(".")
    fun updateExistingList(@Body request: UpdateListRequest): Call<DefaultListResponse>

    @GET("https://s3.ap-south-1.amazonaws.com/quickshoppinglist.com/data/shoppingItems.json")
    fun getSuggestionList(): Call<SuggestionListResponse>

}