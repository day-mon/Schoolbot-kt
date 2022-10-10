package me.damon.schoolbot.commands.main.info

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.JDAInfo
import org.springframework.stereotype.Component
import java.lang.management.ManagementFactory

@Component
class BotInfo : Command(
    name = "BotInfo",
    description = "Shows bot information",
    category = CommandCategory.INFO
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val jda = event.jda
        val runtime = Runtime.getRuntime()

        event.replyEmbed(
            Embed {
                title = "Schoolbot Information"
                url = "https://schoolbot.dev"

                field {
                    name = "JVM Version"
                    value = "${System.getProperty("java.version")} by ${System.getProperty("java.vendor")}"
                    inline = false
                }

                field {
                    name = "JDA Version"
                    value = JDAInfo.VERSION
                    inline = false
                }

                field {
                    name = "Host OS"
                    value = "${System.getProperty("os.name")}  (${System.getProperty("os.arch")}) on v${System.getProperty("os.version")}"
                    inline = false
                }

                field {
                    name = "Memory Usage"
                    value =  "${(runtime.totalMemory() - runtime.freeMemory() shr 20)} MB /  ${runtime.maxMemory() shr 20} MB"
                    inline = false
                }

                field {
                    name = "Thead Count"
                    value = ManagementFactory.getThreadMXBean().threadCount.toString()
                    inline = false
                }

                field {
                    name = "Guild Count"
                    value = jda.guildCache.size().toString()
                    inline = false
                }

                field {
                    name = "User Count"
                    value = jda.guilds.stream().mapToInt { it.memberCount }.sum().toString()
                    inline = false
                }

                field {
                    name = "Bot Version"
                    value = Constants.VERSION
                    inline = false
                }
            }
        )
    }
}