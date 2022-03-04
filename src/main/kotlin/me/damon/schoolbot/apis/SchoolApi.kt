package me.damon.schoolbot.apis

import me.damon.schoolbot.objects.models.SchoolModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SchoolApi
{
    @GET("search")
    suspend fun getSchools(@Query("name") schoolName: String): Response<List<SchoolModel>>
}