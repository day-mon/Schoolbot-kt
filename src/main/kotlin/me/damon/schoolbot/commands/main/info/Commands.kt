package me.damon.schoolbot.commands.main.info

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.entities.MessageEmbed
import java.lang.StringBuilder
import java.time.Instant
import java.util.stream.Collector
import java.util.stream.Collectors

class Commands : Command(
    name = "Commands",
    description = "List all available commands",
    category = CommandCategory.INFO,
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val commands = event.schoolbot.cmd.commands.values
        val pages = mutableListOf<String>()
        commands.stream()
            .collect(Collectors.groupingBy(Command::category))
            .forEach { (category, cmds) ->
                if (cmds.isEmpty()) return@forEach
                val page = StringBuilder()
                    .append("**")
                    .append(category.emoji.getAsChat())
                    .append(" ")
                    .append(category.name)
                    .append("**")

                cmds.forEach {
                    page.append("\n• `")
                        .append("/")
                        .append(it.name.lowercase())
                        .append("` - *")
                        .append(it.description)
                        .append("*")
                }

                pages.add(page.toString())
            }


        val embeds = pages.map {
            Embed {
                description = it
                timestamp = Instant.now()
                author {
                    name = "Commands"
                    url = "https://schoolbot.dev"
                    iconUrl = event.jda.selfUser.defaultAvatarUrl
                }
            }
        }.toList()


        event.sendPaginator(*embeds.toTypedArray())

    }
}