package me.damon.schoolbot.handler

import me.damon.schoolbot.Constants
import me.damon.schoolbot.apis.JohnstownAPI
import me.damon.schoolbot.apis.SchoolApi
import okhttp3.OkHttpClient
import org.springframework.stereotype.Component
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

@Component
class ApiHandler(
    private val schoolbotRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://johnstown.schoolbot.dev/api/")
        .client(Constants.DEFAULT_CLIENT)
        .addConverterFactory(GsonConverterFactory.create())
        .build(),

    private val schoolApiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://universities.hipolabs.com/")
        .client(Constants.DEFAULT_CLIENT)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
)
{
    val johnstownAPI: JohnstownAPI by lazy {
        schoolbotRetrofit.create()
    }

    val schoolApi: SchoolApi by lazy {
        schoolApiRetrofit.create()
    }
}