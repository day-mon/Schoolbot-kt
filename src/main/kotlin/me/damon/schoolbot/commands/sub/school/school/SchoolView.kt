package me.damon.schoolbot.commands.sub.school.school


import me.damon.schoolbot.ext.replyChoiceStringAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import org.springframework.stereotype.Component

@Component
class SchoolView(
    private val schoolService: SchoolService
) : SubCommand(
    name = "view",
    description = "Views all of the schools in a guild",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "school_name",
            description = "Name of the school you want to view",
            autoCompleteEnabled = true,
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        when {
            event.sentWithAnyOptions() ->
            {
                val name = event.getOption<String>("school_name")
                val school = try { schoolService.findSchoolInGuild(event.guildId, name) } catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to find school with the name $name") } ?: return run {
                    event.replyErrorEmbed("School does not exist")
                }

                event.replyEmbed(school.getAsEmbed())

            }
            else ->
            {
                val schools = try { schoolService.findSchoolsInGuild(event.guildId)  } catch (e: Exception) {
                   return  event.replyErrorEmbed("Error occurred while retrieving schools")  }

                if (schools.isEmpty())
                    return event.replyErrorEmbed("There are no schools in `${event.guild.name}`")


                event.sendPaginatorColor(schools)
            }
        }
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)

    {
        val guildId = event.guild?.idLong ?: return logger.error("Guild is null")
        val schools = schoolService.findSchoolsInGuild(guildId)
        event.replyChoiceStringAndLimit(
            schools.map { it.name }
                .filter { it.startsWith(event.focusedOption.value, ignoreCase = true) }
        ).queue()
    }
}