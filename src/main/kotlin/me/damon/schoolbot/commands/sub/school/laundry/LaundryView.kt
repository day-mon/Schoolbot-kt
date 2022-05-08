package me.damon.schoolbot.commands.sub.school.laundry

import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.asCommandChoice
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.interactions.commands.OptionType

class LaundryView : SubCommand(
    name = "view",
    category = CommandCategory.SCHOOL,
    description = "Views laundry in the target dormitory",
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "dormitory",
            choices = Constants.DORMS.map { it.asCommandChoice() }.toList(),
            description = "Target dormitory you want check",
            isRequired = true
        )
    )

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val dorm = event.getOption<String>("dormitory")
        val response = event.schoolbot.apiHandler.johnstownAPI.getLaundryItems(dorm)

        if (!response.isSuccessful)
        {
            logger.error("An error has while attempting to get the response", response.raw().asException())
            event.replyErrorEmbed("An error has occurred while getting the response")
            return
        }

        val models = response.body() ?: return event.replyErrorEmbed("Error has occurred while trying to get the response body")


        event.sendPaginator(models)

    }
}