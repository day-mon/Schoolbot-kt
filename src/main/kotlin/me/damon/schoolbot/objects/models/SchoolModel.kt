package me.damon.schoolbot.objects.models


import com.fasterxml.jackson.annotation.JsonProperty
import me.damon.schoolbot.objects.school.School
import java.time.ZoneId

data class SchoolModel(
    @JsonProperty("alpha_two_code")
    val alphaTwoCode: String, // US

    @JsonProperty("country")
    val country: String, // United States

    @JsonProperty("domains")
    val domains: List<String>,

    @JsonProperty("name")
    val name: String, // Paul D Camp Community College

    @JsonProperty("state-province")
    val stateProvince: String?, // Bangkok
    
    @JsonProperty("web_pages")
    val webPages: List<String>
) {
    fun asSchool(timeZone: ZoneId) = School(
        name = name,
        url = if (webPages.isEmpty()) "https://schoolbot.dev" else webPages[0],
        emailSuffix = if (domains.isEmpty()) "N/A" else domains[0],
        isPittSchool = name.contains("University of Pittsburgh"),
        timeZone = timeZone
    )
}