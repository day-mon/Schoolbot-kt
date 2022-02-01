package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData

class CommandOptionData<in T >(
    val type: OptionType,
    val name: String,
    val description: String,
    val isRequired: Boolean = false,
    val validate: (T) -> Boolean = { true },
)
{
    fun asOptionData(): OptionData = OptionData(type, name, description, isRequired)


}

