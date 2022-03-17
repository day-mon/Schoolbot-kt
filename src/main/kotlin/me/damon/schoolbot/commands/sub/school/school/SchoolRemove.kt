package me.damon.schoolbot.commands.sub.school.school

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.messages.reply_
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
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
            isRequired = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val service = event.schoolbot.schoolService
        val name = event.getOption("school_name")!!.asString
        val schoolResult = service.getSchoolsByGuildId(event.guild.idLong) ?:
           return run { event.replyErrorEmbed("Error occurred while fetching schools ") }

        val schools = schoolResult.filter { it.classes.isEmpty() }

        if (schools.isEmpty()) return run {
            event.replyErrorEmbed("School does not exist.")
        }

        val menuSchools = schools.filter { it.name.contains(name) }

        if (menuSchools.isEmpty()) return run { event.replyErrorEmbed("There are no schools in ${event.guild.name} that contain $name") }
        if (menuSchools.size > 25) return run { event.replyErrorEmbed("Please be more specific. That search propagated ${menuSchools.size} results.") }

        val menu = SelectMenu("schoolDelete:menu") {
            menuSchools.forEachIndexed { index, school ->
                option(
                    school.name,
                    index.toString()
                )
            }
        }
        val selectionEvent = event.sendMenuAndAwait(menu, "Please choose a school you wish to remove")
        val school = menuSchools[selectionEvent.values[0].toInt()]

        selectionEvent.reply_("Are you sure you want to remove ${school.name}").addEmbeds(school.getAsEmbed())
            .addActionRow(getActionRows(selectionEvent, event, school)).queue()
    }

    private fun getActionRows(event: SelectMenuInteractionEvent, cmdEvent: CommandEvent, school: School): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {


            cmdEvent.schoolbot.schoolService.deleteSchool(school, cmdEvent)
            event.hook.editOriginal("School has been deleted")
                .setEmbeds(school.getAsEmbed())
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