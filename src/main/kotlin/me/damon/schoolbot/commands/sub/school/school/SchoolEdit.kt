package me.damon.schoolbot.commands.sub.school.school

import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.send
import me.damon.schoolbot.Constants
import me.damon.schoolbot.bot.Schoolbot
import me.damon.schoolbot.ext.*
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.net.URI
import kotlin.time.Duration.Companion.minutes

class SchoolEdit : SubCommand(
    name = "edit",
    description = "Edits a school",
    memberPermissions = enumSetOf(Permission.MANAGE_ROLES),
    selfPermissions = enumSetOf(Permission.MANAGE_ROLES),
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "school_name",
            description = "Name of school you wish you edit",
            optionType = OptionType.STRING,
            isRequired = true,
            autoCompleteEnabled = true
        ),
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val schoolName = event.getOption<String>("school_name")

        val school = try { event.schoolbot.schoolService.findSchoolInGuild(name = schoolName, guildId = event.guildId)  }
        catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find $name in our database!") } ?: return event.replyErrorEmbed("$name was deleted during/after the autocomplete process!")


        val timeZoneString = Constants.TIMEZONES.values.joinToString(transform = {"**$it**\n"}, separator = String.empty)

        val acknowledgeEmbed = Embed {
            title = "${Emoji.THINKING.getAsChat()} ** WAIT** "
            description = "Two of the attributes in School are a bit difficult to just let you update so let me explain it to you"
            field(name = "1. Timezone", value = "You may pick from these timezones below \n $timeZoneString \n If you choose to switch the timezone all of your reminders (if any) will be adjusted so don't worry ${Emoji.SMILEY.getAsChat()}" , inline = false)
            field(name = "2. Role", value = "The number you will see is the role id. If you want to change the role to something different you can get the role id by going into your server settings > roles > right click role > copy id.", inline = true)
            color = Constants.YELLOW
        }

        val button = event.jda.button(style = ButtonStyle.PRIMARY, "I understand", user = event.user) {
            logger.info("Button clicked | ID: {}", it.interaction.idLong)
            val modal = Modal(id = "school_edit_modal", "Editing ${school.name}") {
                short(label = "School Name", value = school.name, id = "name")
                short(label = "School URL" , value = school.url, id = "url")
                short(label = "School Email Suffix", value = school.emailSuffix, id = "suffix")
                short(label = "School Timezone", value = school.timeZone, id = "timezone")
                short(label = "School Role", value = school.roleId.toString(), id = "role")
            }

            it.message.delete().queue()
            val modalEvent = it.awaitModal(modal, deferReply = true) ?: return@button
            val strBuffer = StringBuffer()
            var errors = 0

            val name = modalEvent.getValue<String>("name")
            name ?: strBuffer.append("${++errors}. Name field cannot be empty").append("\n")
            val url = modalEvent.getValue<URI>("url")
            url ?: strBuffer.append("${++errors}. URL field must be a valid url and/or not blank").append("\n")
            val emailSuffix = modalEvent.getValue<String>("suffix")
            emailSuffix ?: strBuffer.append("${++errors}. Email Suffix field cannot be empty").append("\n")
            if (emailSuffix != null && "@" !in emailSuffix && "@".startsWith(emailSuffix)) strBuffer.append("${++errors}. Email suffix must start with @").append("\n")
            val timeZone = modalEvent.getValue<String>("timezone")
            timeZone ?: strBuffer.append("${++errors}. Timezone field must not be blank").append("\n")
            if (timeZone !in Constants.TIMEZONES.values) strBuffer.append("${++errors}. That timezone was not from the list that I gave you.").append("\n")
            val role = modalEvent.getValue<Role>("role")
            role ?: strBuffer.append("${++errors}. That role doesnt not exist in ${event.guild.name}").append("\n")


            if (strBuffer.isNotEmpty()) return@button it.replyErrorEmbed(strBuffer.toString()).queue()

            val updateName = (schoolName == name).not()

            school.apply {
                this.name = if (updateName) name!! else schoolName
                this.url = url?.toString() ?: "N/A"
                this.emailSuffix = emailSuffix!!
                this.timeZone = timeZone!!
                this.roleId = roleId
            }

            it.hook.send(
                embed = school.getAsEmbed(guild = event.guild),
                content = "Does this look correct?",
                components = getActionRows(event, school, updateName)
            ).queue()
        }
        event.slashEvent.awaitButton(
            embed = acknowledgeEmbed,
            button = button
        ) ?: return
    }

    private fun getActionRows(event: CommandEvent, school: School, updateName: Boolean): Collection<ActionRow>
    {
        val jda = event.jda
        val yes = jda.button(
            style = ButtonStyle.SUCCESS,
            label = "Yes",
            user = event.user,
            expiration = 1.minutes
        ) { button ->
            val service = event.getService<SchoolService>()

            val joke = Constants.JOKES.random()
            button.message.edit(content = "Making changes to ${school.name}. Here's a joke $joke", components = listOf()).queue()
            val updatedSchool = try  { service.update(school) }
            catch (e: Exception) { return@button button.message.editErrorEmbed("Error has occurred while trying to update school").queue() }


            if (updateName) jda.getRoleById(school.roleId)?.manager?.setName(updatedSchool.name)?.queue(null) {
                    button.message.editErrorEmbed("Error while updating role name in server")
                    logger.error("Error occurred while updating role name in {} ", event.guild.name)
                }


            button.message.editMessage("Updates successfully applied ${Emoji.SMILEY.getAsChat()}").queue()
        }

        val no = jda.button(
            style = ButtonStyle.DANGER,
            label = "No",
            user = event.user,
            expiration = 1.minutes
        ) {
            it.message.edit("Okay, if you want you can try again ${Emoji.SMILEY.getAsChat()}", embeds = listOf() , components = listOf()).queue()
        }

        return listOf(yes, no).into()
    }


    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val guildId = event.guild?.idLong ?: return logger.error("Guild is null")
        val schools: List<School> =  schoolbot.schoolService.findSchoolsInGuild(guildId)
        event.replyChoiceStringAndLimit(
            schools.map { it.name }
        ).queue()
    }
}