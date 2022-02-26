package me.damon.schoolbot.objects.command
import net.dv8tion.jda.api.interactions.commands.Command

class CommandChoice(
    private val name: String,
    private val value: String = name
) {

    fun asCommandChoice() = Command.Choice(name, value)
}