package me.damon.schoolbot.commands.sub.school.school

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.ext.editOriginalAndClear
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.time.Duration.Companion.minutes

class SchoolAdd : SubCommand(
    name = "add",
    description = "Adds a school",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "school_name",
            description = "Name of the school you wish to add",
            isRequired = true,
        )
    )
)
{

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val schoolName = event.getOption<String>("school_name")
        event.replyMessage("Searching for `$schoolName`...")
        val response = event.schoolbot.apiHandler.schoolApi.getSchools(schoolName)


        if (!response.isSuccessful)
        {
            logger.error("Unexpected error has occurred", response.raw().asException())
            event.replyErrorEmbed("An unexpected error has occurred!")
            return
        }

        val models = response.body() ?: return event.replyErrorEmbed("Error has occurred while trying to get the response body")



        if (models.isEmpty()) return event.replyErrorEmbed("There are no schools with the name `$schoolName`")
        if (models.size > 25) return event.replyErrorEmbed("Please attempt to narrow your search down. That search propagated `${models.size}` results")


        val menu = SelectMenu("school:menu") {
            models.forEachIndexed { index, schoolModel ->
                option(
                    schoolModel.name, index.toString()
                    //damon, its ryan. u forgot a ';'. not sure if u need in kotlin. but just checking :)-
                    // xd
                )
            }
        }

        val selectionEvent = event.sendMenuAndAwait(menu, "Select an item from the menu to choose a school", acknowledge = true) ?: return
        val school = models[selectionEvent.values[0].toInt()]

        val duplicate = try { event.schoolbot.schoolService.findSchoolInGuild(event.guild.idLong, school.name) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while attempting to check if ${school.name} is a duplicate.. Try again!") }
        if (duplicate != null) return  event.replyErrorEmbed("`${school.name}` already exist. You cannot add duplicate schools!")


        val timeZone = if (school.name.contains("University of Pittsburgh")) "America/New_York"
        else {
            val timeZoneMenu = SelectMenu("timezone:menu") {
                Constants.TIMEZONES.forEach { (k, v) -> option(k, v) }
            }

            val timeZoneSelectionEvent = event.sendMenuAndAwait(timeZoneMenu, "Select a timezone for `${school.name}`. This timezone will be used for class and assignment reminders if you choose to use them.", acknowledge = true) ?: return
            timeZoneSelectionEvent.values[0]
        }

        school.apply {
            this.timeZone = timeZone
        }

        event.hook.editOriginal("Does this look like the correct school?")
            .setEmbeds(school.getAsEmbed())
            .setActionRow(getActionRows(selectionEvent, event, school.asSchool()))
            .queue()
    }


    private fun getActionRows(event: SelectMenuInteractionEvent, cmdEvent: CommandEvent, school: School): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {

            val savedSchool = try
            {
                cmdEvent.service.saveSchool(school, cmdEvent)
            }
            catch (e: Exception)
            {
                logger.error("Error has occurred while trying to save the school", e)
                cmdEvent.replyErrorEmbed("Error has occurred while trying to save school!")
                return@button
            }

            event.hook.editOriginal("School has been saved")
                .setEmbeds(savedSchool.getAsEmbed())
                .setActionRows(emptyList())
                .queue()

        }


        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = event.user, expiration = 1.minutes) {
            event.hook.editOriginalAndClear("Aborting. Thank you for using Schoolbot!")
        }

        return listOf(yes, no)
    }
}