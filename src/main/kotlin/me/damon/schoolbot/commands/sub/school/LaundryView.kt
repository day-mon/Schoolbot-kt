package me.damon.schoolbot.commands.sub.school

import me.damon.schoolbot.constants
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.asCommandChoice
import me.damon.schoolbot.web.asException
import net.dv8tion.jda.api.interactions.commands.OptionType

class LaundryView : SubCommand(
    name = "view",
    category = CommandCategory.SCHOOL,
    description = "Views laundry in the target dormitory",
    options = listOf(
        CommandOptionData<String>(
            type = OptionType.STRING,
            name = "dormitory",
            choices = constants.dorms.map { it.asCommandChoice() }.toList(),
            description = "Target dormitory you want check",
            isRequired = true
        )
    )

)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val dorm = event.getOption("dormitory")?.asString!!
        val response = event.schoolbot.apiHandler.laundryApi.getLaundryItems(dorm)

        if (!response.isSuccessful)
        {
            logger.error("An error has while attempting to get the response", response.raw().asException())
            event.replyMessage("An error has occurred while getting the response")
            return
        }
        val models = response.body() ?: return run {
            event.replyMessage("Error has occurred while trying to get the response body")
        }

        event.sendPaginator(models)

    }
}