package me.damon.schoolbot.handler

import me.damon.schoolbot.Constants
import me.damon.schoolbot.apis.DictionaryAPI
import me.damon.schoolbot.apis.JohnstownAPI
import me.damon.schoolbot.apis.SchoolApi
import org.springframework.stereotype.Component
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

@Component
class ApiHandler(
    private val schoolbotRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://schoolapi.damon.systems/v1/api/")
        .client(Constants.DEFAULT_CLIENT)
        .addConverterFactory(GsonConverterFactory.create())
        .build(),

    private val schoolApiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://universities.hipolabs.com/")
        .client(Constants.DEFAULT_CLIENT)
        .addConverterFactory(GsonConverterFactory.create())
        .build(),

    private val definitionApiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.dictionaryapi.dev/api/v2/entries/")
        .client(Constants.DEFAULT_CLIENT)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

)
{
    val johnstownAPI: JohnstownAPI by lazy {
        schoolbotRetrofit.create()
    }

    val dictionaryApi: DictionaryAPI by lazy {
        definitionApiRetrofit.create()
    }

    val schoolApi: SchoolApi by lazy {
        schoolApiRetrofit.create()
    }
}