package me.damon.schoolbot.apis

import me.damon.schoolbot.objects.models.LaundryModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface LaundryApi
{
    @GET("Laundry/{dorm}")
    suspend fun getLaundryItems(@Path("dorm") dorm: String): Response<List<LaundryModel>>
}