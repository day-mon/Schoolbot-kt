package me.damon.schoolbot.commands.sub.school.course

import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand

class CourseRemove : SubCommand(
    name = "remove",
    description = "Removes a course from a guild",
    category = CommandCategory.SCHOOL
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        /*
        val courses =
            event.schoolbot.schoolService.getClassesInGuild(event.guildId)?.filter { it.assignments.isEmpty() }
                ?: return run {
                    event.replyErrorEmbed("Error occurred while attempting to grab the courses for the `${event.guild.name}`")
                }

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
        )

        val course = courses[selection.values[0].toInt()]



        selection.hook.editMessageById(selection.messageId,"Are you sure you want to remove `${course.name}` from `${course.school.name}`")
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

         */
    }
}