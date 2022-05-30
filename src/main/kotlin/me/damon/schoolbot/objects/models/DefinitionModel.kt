package me.damon.schoolbot.objects.models

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed

data class DefinitionModel (
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic>,
    val meanings: List<Meaning>,
    val license: License,
    val sourceUrls: List<String>
) : Pagable {

    override fun getAsEmbed(): MessageEmbed = Embed {
        title = word
        url = if (phonetics.isEmpty()) "https://schoolbot.dev" else phonetics.first().audio
        description = "*${phonetic ?: "No phonetic found"}*"

        val count = meanings.size
        for ((index, k) in meanings.withIndex())
        {
            field("Part of Speech", k.partOfSpeech, false)
            for ((innerIndex, i) in meanings[index].definitions.withIndex())
            {
                val number = innerIndex + 1
                field(name = "Meaning #$number", value = i.definition, inline = false)
                field(name = "Example #$number", value = i.example?.replace(word, "`${word}`") ?: "No example provided for this definition")
                if (i.synonyms.isEmpty())
                    field(name = "Synonyms", value = "No Synonyms found")
                else
                {
                    val synString = i.synonyms.joinToString { it.toString() }
                    field(name = "Synonyms", value = if (synString.length > MessageEmbed.VALUE_MAX_LENGTH) i.synonyms.joinToString(limit = 10) else synString)
                }

                if (i.antonyms.isEmpty())
                    field(name = "Antonyms", value = "No antonyms found")
                else
                {
                    val ayn = i.antonyms.joinToString { it.toString() }
                    field(name = "Synonyms", value = if (ayn.length > MessageEmbed.VALUE_MAX_LENGTH) i.antonyms.joinToString(limit = 10) else ayn)
                }
            }
            if (index != count - 1) field(name = "", value = "", inline = false)
        }
        footer { name = "Source(s) - ${sourceUrls.joinToString(limit = 10)}"}
    }
}

data class License (
    val name: String,
    val url: String
)

data class Meaning (
    val partOfSpeech: String,
    val definitions: List<Definition>,
    val synonyms: List<String?>,
    val antonyms: List<String?>
)

data class Definition (
    val definition: String,
    val synonyms: List<String?>,
    val antonyms: List<String?>,
    val example: String? = null
)

data class Phonetic (
    val text: String,
    val audio: String,
    val sourceURL: String,
    val license: License
)