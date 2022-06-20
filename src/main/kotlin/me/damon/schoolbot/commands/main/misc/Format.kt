package me.damon.schoolbot.commands.main.misc

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import org.springframework.stereotype.Component

@Component
class Format : Command(
    name = "Format",
    category = CommandCategory.MISC,
    description = "Visual representation on how to format code using discord"
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val formatEmbed = Embed {
            title = "How to format!"
            description = """
                     Surround code with:
                             \`\`\` kt
                            val p = Person() \`\`\`
                            This should display:
                            ```kt
                     val p = Person() ```
                     Replace 'kt' with the alphabetic character (in lower case) of another language. For example: C++ -> cpp, Python -> python or  py
                                                    
                     This character can be found at the top left of your keyboard!
                """.trimIndent()
        }
        event.replyEmbed(formatEmbed)
    }
}