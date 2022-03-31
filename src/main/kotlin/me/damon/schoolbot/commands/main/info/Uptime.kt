package me.damon.schoolbot.commands.main.info

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.Instant


class Uptime : Command(
    name = "Uptime",
    category = CommandCategory.INFO,
    description = "Displays uptime"
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        //  ManagementFactory.getRuntimeMXBean().uptime gets JVM uptime in milliseconds
        val time = Duration.ofMillis(ManagementFactory.getRuntimeMXBean().uptime)
        event.replyEmbed(
            EmbedBuilder()
                .setTitle("Uptime")
                .setDescription("${time.toDaysPart()} days, ${time.toHoursPart()} hours, ${time.toMinutesPart()} minutes, ${time.toSecondsPart()} seconds")
                .build()
        )
    }
}