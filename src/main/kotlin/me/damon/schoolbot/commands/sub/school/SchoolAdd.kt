
package me.damon.schoolbot.commands.sub.school

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.messages.reply_
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.web.asException
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration.Companion.minutes

class SchoolAdd : SubCommand(
    name = "add",
    description = "Adds a school",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            type = OptionType.STRING,
            name = "school_name",
            description = "Name of the school you wish to add",
            isRequired = true,
            autoCompleteEnabled = true
        )
    )
)
{

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val schoolName = event.getOption("school_name")!!.asString
        event.replyMessage("Searching for `$schoolName`...")
        val response = event.schoolbot.apiHandler.schoolApi.getSchools(schoolName)


        if (!response.isSuccessful)
        {
            logger.error("Unexpected error has occurred",  response.raw().asException())
            event.replyErrorEmbed("An unexpected error has occurred!")
            return
        }

        val models = response.body() ?: return run {
            event.replyErrorEmbed("Error has occurred while trying to get the response body")
        }


        if (models.isEmpty()) return run { event.replyErrorEmbed("There are no schools with the name `$schoolName`") }
        if (models.size > 25) return run { event.replyErrorEmbed("Please attempt to narrow your search down. That search propagated `${models.size}` results") }

        val menu = SelectMenu("school:menu") { models.forEachIndexed { index, schoolModel -> option(schoolModel.name, index.toString()) } }

        val selectionEvent = event.sendMenuAndAwait(menu, "Select an item from the menu to choose a school  **Timeout is set to one minute**")
        val school = models[selectionEvent.values[0].toInt()]
        selectionEvent.reply_("Does this look like the correct school?")
            .addEmbeds(school.getAsEmbed())
            .addActionRow(getActionRows(selectionEvent, event, school.asSchool(ZoneId.systemDefault())))
            .queue()
    }


    private fun getActionRows(event: SelectMenuInteractionEvent, cmdEvent: CommandEvent, school: School): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {

            try
            {
                val savedSchool = cmdEvent.schoolbot.schoolService.saveSchool(school, cmdEvent)
                it.reply("School has been saved")
                    .addEmbeds(savedSchool.getAsEmbed())
                    .queue()
            }
            catch (e: IllegalArgumentException)
            {
                logger.error("{} could not be saved", school.name)
                cmdEvent.replyMessage("${school.name} could not be saved")
            }

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