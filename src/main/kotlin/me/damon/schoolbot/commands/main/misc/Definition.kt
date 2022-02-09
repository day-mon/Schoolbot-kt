package me.damon.schoolbot.commands.main.misc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.models.DefinitionModel
import me.damon.schoolbot.web.asException
import me.damon.schoolbot.web.await
import me.damon.schoolbot.web.bodyAsString
import me.damon.schoolbot.web.get
import net.dv8tion.jda.api.interactions.commands.OptionType

class Definition : Command(
    name = "Define",
    category = CommandCategory.MISC,
    description = "Gives a definition of a word that is specified",
    options = listOf(
            CommandOptionData<String>(
            type = OptionType.STRING,
            name = "word",
            description = "The word you are looking for a definition of",
            isRequired = true,
                validate =  { inner -> inner.all { it.isLetter() } },
                failedValidation = "This argument only takes in alphabetic characters"
        )
    ),

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val word = event.getOption("word")!!.asString
        val regex = Regex("\\s")
        val dictionaryUrl = "https://api.dictionaryapi.dev/api/v2/entries/en/${word.replace(regex, "%20")}"
        val client = event.jda.httpClient
        val request = get(dictionaryUrl)



        client.newCall(request).await(scope = event.scope) { response ->

            when
            {
                response.isSuccessful -> {
                    val json = response.bodyAsString() ?: return@await run {
                        logger.error("Response body could not be returned")
                        event.replyMessage("Response from external api returned a bad response.")
                    }


                    val models: List<DefinitionModel> = jacksonObjectMapper().readValue(json)
                    event.sendPaginator(*models.map { it.getAsEmbed() }.toTypedArray())

                }

                response.code() == 404 -> {
                    event.replyMessage("**$word** is not a valid english word")
                }

                response.code() >= 500 -> {
                    logger.error("API has responded a internal server error", response.asException())
                    event.replyMessage("Dictionary API has responded a internal server error")
                }

                else -> {
                    logger.error("An unexpected error has occurred", response.asException())
                    event.replyMessage("An unexpected error has occurred")
                }
            }
        }
    }
}