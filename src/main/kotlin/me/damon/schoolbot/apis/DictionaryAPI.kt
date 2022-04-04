package me.damon.schoolbot.apis

import me.damon.schoolbot.objects.models.DefinitionModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryAPI
{
    @GET("{locale}/{word}")
    suspend fun getDefinition(@Path("locale") locale: String, @Path("word") word: String): Response<List<DefinitionModel>>
}

