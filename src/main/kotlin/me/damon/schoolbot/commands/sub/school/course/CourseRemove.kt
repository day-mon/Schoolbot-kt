package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import me.damon.schoolbot.ext.replyErrorEmbed
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.service.CourseService
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
        val courseService = event.getService<CourseService>()
        val courses =
           try { courseService.findEmptyClassesInGuild(event.guild.idLong) }
           catch (e: Exception) { event.replyErrorEmbed("Error occurred while attempting to grab the courses for the `${event.guild.name}`"); return }


        if (courses.isEmpty()) return run {
            event.replyErrorEmbed("There are no courses with no assignments in the `${event.guild.name}`")
        }

        val selection = event.sendMenuAndAwait(
            menu = SelectMenu("${event.slashEvent.idLong}:CRcourseselection:menu") {
                courses.forEachIndexed { index, course ->
                    option(
                        course.name,
                        index.toString()
                    )
                }
            },

            message = "Please select the course you wish to remove"
        ) ?: return

        val course = courses[selection.values[0].toInt()]



        selection.reply("Are you sure you want to remove `${course.name}` from `${course.school.name}`")
            .addActionRow(getActionRows(event, selection, course, courseService))
            .queue()

    }

    private fun getActionRows(cmdEvent: CommandEvent, event: SelectMenuInteractionEvent, course: Course, service: CourseService): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {
            try { service.deleteCourse(course, cmdEvent) } catch (e: Exception) {
                event.hook.replyErrorEmbed(body = "Error occurred while trying delete ${course.name}")
                return@button
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