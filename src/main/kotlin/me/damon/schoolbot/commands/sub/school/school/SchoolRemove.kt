package me.damon.schoolbot.commands.sub.school.school

import dev.minn.jda.ktx.interactions.components.button

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.*
import kotlin.time.Duration.Companion.minutes

class SchoolRemove : SubCommand(
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
        val service = event.getService<SchoolService>()

        val schoolId = event.getOption<String>("school_name")
        val id = UUID.fromString(schoolId)
        val school = service.findSchoolById(id)

        if (school == null)
        {
            event.replyErrorEmbed("${Emoji.ERROR} School not found")
            return
        }

        event.hook.editOriginal("Are you sure you want to remove ${school.name}")
            .setEmbeds(school.getAsEmbed())
            .setActionRow(getActionRows( event, school))
            .queue()
    }

    private suspend fun getActionRows( cmdEvent: CommandEvent, school: School): List<Button>
    {
        val hook = cmdEvent.hook
        val jda = cmdEvent.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = cmdEvent.user, expiration = 1.minutes) {


            cmdEvent.schoolbot.schoolService.deleteSchool(school, cmdEvent)
            val embed = withContext(Dispatchers.IO) { school.getAsEmbed() }
            hook.editOriginal("School has been deleted")
                .setEmbeds(embed)
                .setActionRows(Collections.emptyList())
                .queue()


        }

        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = cmdEvent.user, expiration = 1.minutes) {
            hook.editOriginal("Aborting.. Thank you for using Schoolbot!")
                .setActionRows(Collections.emptyList())
                .setEmbeds(Collections.emptyList())
                .queue()
        }

        return listOf(yes, no)
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val schools = schoolbot.schoolService.findSchoolsWithNoClasses(guildId = event.guild!!.idLong) ?: return
        event.replyChoiceAndLimit(
            schools.map { Choice(it.name, it.id.toString()) },
        ).queue()
    }

}