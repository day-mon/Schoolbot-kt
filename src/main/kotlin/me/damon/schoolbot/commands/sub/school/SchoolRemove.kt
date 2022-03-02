@file:OptIn(ExperimentalTime::class)

package me.damon.schoolbot.commands.sub.school

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import kotlin.time.ExperimentalTime

class SchoolRemove : SubCommand(
    name = "remove",
    description = "Removes a school",
    category = CommandCategory.SCHOOL
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val service = event.schoolbot.schoolService
        val schools = service.getSchoolsByGuildId(event.guild.idLong).filter { it.classes.isEmpty() }

        if (schools.isEmpty()) return run {
            event.replyEmbed(
                Embed {
                    title = "Error has occurred"
                    description = "School does not exist"
                    color = 0
                }
            )
        }

       // val menu = SelectMenu("schoolDelete:menu") { schools.forEachIndexed { index, school -> option(school.name, index.toString()) } }

    }
}