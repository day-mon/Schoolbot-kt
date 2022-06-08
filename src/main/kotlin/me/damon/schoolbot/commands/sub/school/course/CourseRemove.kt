package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.into
import me.damon.schoolbot.ext.replyErrorEmbed
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.service.CourseService
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class CourseRemove(
    private val courseService: CourseService
) : SubCommand(
    name = "remove",
    description = "Removes a course from a guild",
    category = CommandCategory.SCHOOL
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val courses =
           try { courseService.findEmptyAssignmentsInGuild(event.guild.idLong) }
           catch (e: Exception) { event.replyErrorEmbed("Error occurred while attempting to grab the courses for the `${event.guild.name}`"); return }


        if (courses.isEmpty()) return run {
            event.replyErrorEmbed("There are no courses with no assignments in the `${event.guild.name}`")
        }

        val selection = event.awaitMenu(
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

        val course = courses[selection.values.first().toInt()]



        selection.reply("Are you sure you want to remove `${course.name}` from `${course.school.name}`")
            .addActionRows(getActionRows(event.guild, selection, course, courseService))
            .queue()

    }

    private fun getActionRows(guild: Guild, event: SelectMenuInteractionEvent, course: Course, service: CourseService): List<ActionRow>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {
            try { service.deleteCourse(course, guild) }
            catch (e: Exception)
            {
                event.hook.replyErrorEmbed(body = "Error occurred while trying delete ${course.name}")
                return@button
            }
            event.hook.editOriginal("Course has been deleted successfully")
                .setEmbeds(course.getAsEmbed())
                .setActionRows(emptyList())
                .queue()
        }

        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = event.user, expiration = 1.minutes) {
            event.hook.editOriginal("Aborting.. Thank you for using Schoolbot!")
                .setActionRows(emptyList())
                .setEmbeds(emptyList())
                .queue()
        }

        return listOf(yes, no).into()
    }
}