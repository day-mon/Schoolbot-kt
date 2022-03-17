package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.interactions.option
import me.damon.schoolbot.ext.replyErrorEmbed
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.Course
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.*
import kotlin.time.Duration.Companion.minutes

class CourseRemove : SubCommand(
    name = "remove",
    description = "Removes a course from a guild",
    category = CommandCategory.SCHOOL
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val schools =
            event.schoolbot.schoolService.getSchoolsByGuildId(event.guildId)?.filter { it.classes.isNotEmpty() }
                ?: return run {
                    event.replyErrorEmbed("Error occurred while attempting to grab the schools for the `${event.guild.name}`")
                }

        if (schools.isEmpty()) return run {
            event.replyErrorEmbed("There are no schools with courses in `${event.guild.name}`")
        }

        val selection = event.sendMenuAndAwait(
            menu = SelectMenu("${event.slashEvent.idLong}:CRschoolselection:menu") {
                schools.forEachIndexed { index, school ->
                    option(
                        school.name,
                        index.toString()
                    )
                }
            },

            message = "Please select a school you to remove the school from"
        )

        val school = schools[selection.values[0].toInt()]

        val courses = school.classes.filter {
            it.assignments.isEmpty()
        }

        if (courses.isEmpty()) return run {
            event.replyErrorEmbed("There are no courses in `${school.name}`")
        }


        val courseSelection = event.sendMenuAndAwait(
            menu = SelectMenu("${event.slashEvent.idLong}:CRcourseslection:menu") {
                courses.forEachIndexed { index, course ->
                    option(
                        course.name,
                        index.toString()
                    )
                }
            },

            message = "Please select a course to remove"
        )

        val course = courses[courseSelection.values[0].toInt()]

        selection.hook.editOriginal("Are you sure you want to remove ${course.name} from ${school.name}")
            .setActionRow(getActionRows(event, selection, course))
            .queue()

    }

    private fun getActionRows(cmdEvent: CommandEvent, event: SelectMenuInteractionEvent, course: Course): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {
            cmdEvent.schoolbot.schoolService.deleteCourse(course, cmdEvent) ?: return@button run {
                event.hook.replyErrorEmbed(body = "Error occurred while trying delete ${course.name}")
            }
            event.hook.editOriginal("Course has been deleted successfully")
                .setEmbeds(course.getAsEmbed())
                .setActionRows(Collections.emptyList())
                .queue()
        }

        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = event.user, expiration = 1.minutes) {
            event.hook.editOriginal("Aborting.. Thank you for using Schoolbot!")
                .setActionRows(Collections.emptyList())
                .setEmbeds(Collections.emptyList())
                .queue()
        }

        return listOf(yes, no)
    }
}