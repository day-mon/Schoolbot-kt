package me.damon.schoolbot.commands.sub.school.school

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.editMessage_
import dev.minn.jda.ktx.messages.into
import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.*
import me.damon.schoolbot.handler.ApiHandler
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class SchoolAdd(
    private val apiHandler: ApiHandler,
    private val schoolService: SchoolService
) : SubCommand(
    name = "add",
    description = "Adds a school",
    category = CommandCategory.SCHOOL,
    selfPermissions = enumSetOf(Permission.MANAGE_ROLES),
    memberPermissions = enumSetOf(Permission.MANAGE_ROLES),
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
        val response = apiHandler.schoolApi.getSchools(schoolName)


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

        val selectionEvent = event.awaitMenu(menu, "Select an item from the menu to choose a school", acknowledge = true, deleteAfter = true) ?: return
        val school = models[selectionEvent.values.first().toInt()]

        val duplicate = try { schoolService.findSchoolInGuild(event.guild.idLong, school.name) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while attempting to check if ${school.name} is a duplicate.. Try again!") }
        if (duplicate != null) return  event.replyErrorEmbed("`${school.name}` already exist. You cannot add duplicate schools!")


        val timeZone = if (school.name.contains("University of Pittsburgh")) "America/New_York"
        else
        {
            val timeZoneMenu = SelectMenu("timezone:menu") {
                Constants.TIMEZONES.forEach { (k, v) -> option(k, v) }
            }
            val timeZoneSelectionEvent = event.awaitMenu(timeZoneMenu, "Select a timezone for `${school.name}`. This timezone will be used for class and assignment reminders if you choose to use them.", acknowledge = true, deleteAfter = true) ?: return
            timeZoneSelectionEvent.values.first()
        }

        school.apply {
            this.timeZone = timeZone
        }

        selectionEvent.send(content = "Does this look like the correct school?", embeds = listOf(school.getAsEmbed()), actionRows = getActionRows(selectionEvent, event, school.asSchool()))
    }


    private fun getActionRows(event: SelectMenuInteractionEvent, cmdEvent: CommandEvent, school: School): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {

            it.editMessage_(content = "Adding `${school.name}` to the database...",  components = listOf()).queue()
            val savedSchool = try
            {
                schoolService.saveSchool(school, cmdEvent)
            }
            catch (e: Exception)
            {
                logger.error("Error has occurred while trying to save the school", e)
                return@button it.replyErrorEmbed(errorString = "Error has occurred while trying to save school!").queue()
            }

            it.hook.editOriginal("Successfully added `${school.name}` to the database!")
                .setEmbeds(listOf(savedSchool.getAsEmbed()))
                .queue()
        }


        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = event.user, expiration = 1.minutes) {
            event.hook.editOriginalAndClear("Aborting. Thank you for using Schoolbot!")
        }

        return listOf(yes, no)
    }
}