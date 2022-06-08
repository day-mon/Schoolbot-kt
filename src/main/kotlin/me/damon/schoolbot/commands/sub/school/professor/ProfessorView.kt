package me.damon.schoolbot.commands.sub.school.professor


import me.damon.schoolbot.ext.replyChoiceAndLimit
import me.damon.schoolbot.ext.toUUID
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.service.ProfessorService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.stereotype.Component

@Component
class ProfessorView(
    private val professorService: ProfessorService,
    private val schoolService: SchoolService
) : SubCommand(
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



        val professors = try { professorService.findBySchoolId(name) }
        catch (e: Exception) { return event.replyErrorEmbed("An error occurred while trying to find professors") }

        if (professors.isEmpty()) return event.replyErrorEmbed("No professors found with the name `$name`")

        event.sendPaginator(professors)
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)

    {
        val guildId = event.guild?.idLong ?: return logger.warn("Guild ID was null when trying to auto complete")
        val schools = try { schoolService.findSchoolsInGuild(guildId) } catch (e: Exception) {
            return logger.warn("An error occurred while trying to find professors", e)
        }

        event.replyChoiceAndLimit(schools.map { Command.Choice(it.name, it.id.toString()) }).queue()
    }
}