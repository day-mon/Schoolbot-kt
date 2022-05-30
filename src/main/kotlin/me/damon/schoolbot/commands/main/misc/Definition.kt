package me.damon.schoolbot.commands.main.misc

import me.damon.schoolbot.handler.ApiHandler
import me.damon.schoolbot.objects.command.*
import net.dv8tion.jda.api.interactions.commands.OptionType

class Definition : Command(
    name = "Define",
    category = CommandCategory.MISC,
    description = "Gives a definition of a word that is specified",
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "word",
            description = "The word you are looking for a definition of",
            isRequired = true,
            validate = { inner -> inner.all { it.isLetter() } },
            failedValidation = "This argument only takes in alphabetic characters"
        ),

        CommandOptionData(
            name = "locale",
            optionType = OptionType.STRING,
            description = "The locale to use for the definition",
            choices = listOf(
                CommandChoice(name = "English", value = "en"),
                CommandChoice(name = "Spanish", value = "es"),
                CommandChoice(name = "French", value = "fr")

            )
        )),
    )
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val word = event.getOption<String>("word")
        val locale = event.getOption<String>("locale") ?: "en"
        val response = event.getHandler<ApiHandler>().dictionaryApi.getDefinition(locale, word)
        if (response.code() == 404) return run { event.replyErrorEmbed("Could not find definition for `${word}`") }
        if (!response.isSuccessful) return run { event.replyErrorEmbed("Response to dictionary api failed") }
        val models = response.body() ?: return run { event.replyErrorEmbed("Error while retrieving the response body") }
        event.sendPaginator(models)
    }
}