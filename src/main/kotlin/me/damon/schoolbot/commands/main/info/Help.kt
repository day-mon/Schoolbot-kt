package me.damon.schoolbot.commands.main.info

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.interactions.commands.OptionType

class Help : Command(
    name = "Help",
    description = "Provides you with help with certain commands",
    category = CommandCategory.INFO,
    options = listOf(
        CommandOptionData<String>(
            type = OptionType.STRING,
            name = "command",
            description = "Name of the command you wish to have assistance with",
            isRequired = true
        )
    )

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        TODO("Not yet implemented")
    }
}