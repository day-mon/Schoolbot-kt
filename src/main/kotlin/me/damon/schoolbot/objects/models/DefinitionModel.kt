package me.damon.schoolbot.objects.models

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagintable
import net.dv8tion.jda.api.entities.MessageEmbed

data class DefinitionModel(
    var index: Int?,
    val word: String,
    val phonetic: String?,
    val phonetics: List<Phonetic>,
    val origin: String?,
    val meanings: List<Meaning>,
) : Pagintable {
    override fun getAsEmbed(): MessageEmbed
    {



        return Embed {
            title = word
            url = if(phonetics.isEmpty()) "https://schoolbot.dev" else "https://${phonetics[0].audio}"
            description = "*${phonetic ?: "No phonetic found"}*"

            field {
                name = "Origin"
                value = origin ?: "Origin not found"
                inline = false
            }


            for ((index, i) in meanings[0].definitions.withIndex())
            {
                val number = index + 1
                field {
                    name = "Meaning #$number"
                    value = i.definition
                }

                field {
                    name = "Example #$number"
                    value = i.example?.replace(word, "`${word}`") ?: "No example provided for this definition"
                }

                field {
                    if (i.synonyms.isEmpty())
                    {
                        name = "Synonyms"
                        value = "No synonyms found"
                        inline = false
                        return@field
                    }

                    val syn = i.synonyms.joinToString { it }
                    name = "Synonyms"
                    value = if (syn.length > MessageEmbed.VALUE_MAX_LENGTH) i.synonyms.joinToString (limit =  10) else syn
                    inline = false
                }

                field {
                    if (i.antonyms.isEmpty())
                    {
                        name = "Antonyms"
                        value = "No antonyms found"
                        inline = false
                        return@field
                    }

                    val ayn =  i.antonyms.joinToString { it.toString() }
                    name = "Antonyms"
                    value = if (ayn.length > MessageEmbed.VALUE_MAX_LENGTH) i.antonyms.joinToString (limit =  10) else ayn
                    inline = false
                }
            }
        }
    }
}

data class Phonetic(
    val text: String?,
    val audio: String?,
)

data class Meaning(
    val partOfSpeech: String?,
    val definitions: List<Definition>,
)

data class Definition(
    val definition: String,
    val example: String?,
    val synonyms: List<String>,
    val antonyms: List<Any?>,
)
