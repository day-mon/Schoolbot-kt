package me.damon.schoolbot.commands.sub.school.school

import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.replyChoiceStringAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType

class SchoolView : SubCommand(
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
                val school = event.schoolbot.schoolService.findSchoolInGuild(event.guildId, name) ?: return run {
                    event.replyErrorEmbed("School does not exist")
                }

                event.replyEmbed(school.getAsEmbed())

            }
            else ->
            {
                val schools = event.schoolbot.schoolService.getSchoolsByGuildId(event.guildId) ?:
                   return run { event.replyErrorEmbed("Error occurred while retrieving schools") }

                if (schools.isEmpty()) return run {
                    event.replyErrorEmbed("There are no schools in `${event.guild.name}`")
                }

                event.sendPaginatorColor(schools)
            }
        }
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
       val schools = schoolbot.schoolService.getSchoolsByGuildId(event.guild!!.idLong) ?: return
        event.replyChoiceStringAndLimit(
            schools.map { it.name }
                .filter { it.startsWith(event.focusedOption.value, ignoreCase = true) }
        ).queue()
    }
}