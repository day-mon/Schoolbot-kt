package me.damon.schoolbot.objects.models

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed

data class DefinitionModel(
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic?>,
    val meanings: List<Meaning?>,
    val license: License2,
    val sourceUrls: List<String?>,
) : Pagable {
    //todo fix
    override fun getAsEmbed(): MessageEmbed
    {
        return Embed {
            title = word
            description = "*$phonetic*"
        }
    }
}

data class Phonetic(
    val text: String,
    val audio: String,
    val sourceUrl: String?,
    val license: License?,
)

data class License(
    val name: String,
    val url: String,
)

data class Meaning(
    val partOfSpeech: String,
    val definitions: List<Definition>,
)

data class Definition(
    val definition: String,
    val example: String?,
)

data class License2(
    val name: String,
    val url: String,
)
