package me.damon.schoolbot.handler

import me.damon.schoolbot.apis.JohnstownAPI
import me.damon.schoolbot.apis.SchoolApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class ApiHandler(
    private val schoolbotRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://johnstown.schoolbot.dev/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build(),

    private val schoolApiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://universities.hipolabs.com/")
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