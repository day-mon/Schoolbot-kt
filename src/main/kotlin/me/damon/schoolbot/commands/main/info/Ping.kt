package me.damon.schoolbot.commands.main.info

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class Ping : Command(
    name = "Ping",
    description = "Responds with rest and gateway ping",
    category = CommandCategory.INFO,
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val jda = event.jda

        jda.restPing
            .queue {
                event.replyEmbed(
                    EmbedBuilder()
                        .addField("Gateway Ping", "${jda.gatewayPing} ms", false)
                        .addField("Rest Ping", "$it ms", false)
                        .build()
            )
            }
    }
}