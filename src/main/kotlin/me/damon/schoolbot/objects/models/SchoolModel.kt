package me.damon.schoolbot.objects.models


import com.fasterxml.jackson.annotation.JsonProperty
import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagintable
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.entities.MessageEmbed
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
) : Pagintable {
    fun asSchool(timeZone: ZoneId) = School(
        name = name,
        url = if (webPages.isEmpty()) "https://schoolbot.dev" else webPages[0],
        emailSuffix = if (domains.isEmpty()) "N/A" else domains[0],
        isPittSchool = name.contains("University of Pittsburgh"),
        timeZone = timeZone
    )

    override fun getAsEmbed(): MessageEmbed = Embed {
        title = name
        field {
            name = "Location"
            value = "$country, $stateProvince"
            inline = false
        }
        field {
            name = "Web Pages"
            value = if (webPages.isEmpty()) "N/A" else webPages.joinToString { it }
            inline = false
        }
        field {
            name = "Domains"
            value = if (domains.isEmpty()) "N/A" else domains.joinToString { it }
            inline = false
        }
    }
}