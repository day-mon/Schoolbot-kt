package me.damon.schoolbot.objects.misc

import net.dv8tion.jda.api.interactions.commands.Command

class BasicAutocompletionChoice(
    val name: String,
    val value: String
)
{
    fun toCommandAutocompleteChoice(): Command.Choice = Command.Choice(name, value)
}