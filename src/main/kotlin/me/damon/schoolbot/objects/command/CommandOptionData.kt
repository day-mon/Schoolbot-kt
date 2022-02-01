package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData


inline fun <reified T> CommandOptionData(type: OptionType, name: String, description: String, isRequired: Boolean = false, noinline validate: (T) -> Boolean = { true })
        = CommandOptionData(T::class.java, type, name, description, isRequired, validate)

class CommandOptionData<T>(
     val type: Class<T>,
     val optionType: OptionType,
    val name: String,
     val description: String,
     val isRequired: Boolean = false,
    val validate: (T) -> Boolean = {true},
)
{

    fun asOptionData(): OptionData = OptionData(optionType, name, description, isRequired)





}


