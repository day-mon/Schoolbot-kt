package me.damon.schoolbot.commands.sub.school.professor

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.ext.toUUID
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.service.ProfessorService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType

class ProfessorView : SubCommand(
    name = "view",
    description = "Views all professors in a given school",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            name = "school_name",
            description = "Name of the school you wish to view professors from",
            optionType = OptionType.STRING,
            isRequired = true,
            autoCompleteEnabled = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val name = event.getOption<String>("school_name").toUUID()
            ?: return event.replyErrorEmbed("School has not been found")


        val service = event.getService<ProfessorService>()

        val professors = try { service.findBySchoolId(name) }
        catch (e: Exception) { return event.replyErrorEmbed("An error occurred while trying to find professors") }

        if (professors.isEmpty()) return event.replyErrorEmbed("No professors found with the name `$name`")

        event.sendPaginator(professors)
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val guildId = event.guild?.idLong ?: return logger.warn("Guild ID was null when trying to auto complete")
        val schools = try { schoolbot.schoolService.findSchoolsInGuild(guildId) } catch (e: Exception) {
            return logger.warn("An error occurred while trying to find professors", e)
        }

        event.replyChoiceAndLimit(schools.map { Command.Choice(it.name, it.id.toString()) }).queue()
    }
}