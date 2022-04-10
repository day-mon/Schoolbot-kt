package me.damon.schoolbot.apis

import me.damon.schoolbot.objects.models.CourseModel
import me.damon.schoolbot.objects.models.LaundryModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface JohnstownAPI
{
    @GET("laundry/{dorm}")
    suspend fun getLaundryItems(@Path("dorm") dorm: String): Response<List<LaundryModel>>

    @GET("course/{term}/{number}")
    suspend fun getCourse(@Path("term") term: String, @Path("number")number: String): Response<CourseModel>

}