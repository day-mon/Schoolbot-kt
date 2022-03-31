package me.damon.schoolbot

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object Constants
{
    val dorms = listOf(
        "willow",
        "hickory",
        "buckhorn",
        "llc",
        "oak",
        "maple",
        "heather",
        "hawthorn",
        "hemlock",
        "maple",
        "laurel",
        "larkspur",
        "cpas",
    )

    const val RED = 0x990f0f
    const val MAX_ROLE_COUNT = 250
    const val MAX_CHANNEL_COUNT = 500
    val DEFAULT_CLIENT: OkHttpClient =
        OkHttpClient.Builder()
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60 , TimeUnit.SECONDS)
            .build()
}
