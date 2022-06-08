package me.damon.schoolbot.commands.sub.school.school

import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Component
class SchoolRemove(
    private val schoolService: SchoolService,
    private val courseService: CourseService
) : SubCommand(
    name = "remove",
    description = "Removes a school",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "school_name",
            description = "Name of school you want to remove",
            isRequired = true,
            autoCompleteEnabled = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {

        val schoolId = event.getOption<String>("school_name")
        val id = try { UUID.fromString(schoolId) } catch (e: Exception) { return event.replyErrorEmbed("That is not a valid school to be deleted at this time.\nIn order for a school to be deleted it must have no classes and be added to your guild")}
        val school = try {  schoolService.findSchoolById(id) } catch (e: Exception) { return  event.replyErrorEmbed("Error occurred while searching for school")} ?: return event.replyErrorEmbed("${Emoji.ERROR} School not found")

        val courses = try { courseService.findBySchool(school) } catch (e: Exception) { return event.replyErrorEmbed("Error occurred while checking if this school has any courses. \n Why would we check? Idk some of you are sneaky.")}
        if (courses.isNotEmpty()) return event.replyErrorEmbed("School has courses. Remove them first. Nice try.")

        event.hook.send(content = "Are you sure you want to remove ${school.name}", embed = school.getAsEmbed(), components = getActionRows(event, school)).queue()

    }

    private suspend fun getActionRows( cmdEvent: CommandEvent, school: School): List<ActionRow>
    {
        val hook = cmdEvent.hook
        val jda = cmdEvent.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = cmdEvent.user, expiration = 1.minutes) {

            try { schoolService.deleteSchool(school, cmdEvent) } catch (e: Exception) { return@button cmdEvent.replyErrorEmbed("Error occurred while trying to delete school") }
            val embed = withContext(Dispatchers.IO) { school.getAsEmbed() }
            hook.editOriginal("School has been deleted")
                .setEmbeds(embed)
                .setActionRows(emptyList())
                .queue()


        }

        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = cmdEvent.user, expiration = 1.minutes) {
            hook.editOriginal("Aborting.. Thank you for using Schoolbot!")
                .setActionRows(emptyList())
                .setEmbeds(emptyList())
                .queue()
        }

        return listOf(yes, no).into()
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)

    {
        val guildId = event.guild?.idLong ?: return logger.error("Event should have not been processed in a non-guild environment")
        val schools = schoolService.findSchoolsWithNoClasses(guildId)
        event.replyChoiceAndLimit(
            schools.map { Choice(it.name, it.id.toString()) },
        ).queue()
    }

}