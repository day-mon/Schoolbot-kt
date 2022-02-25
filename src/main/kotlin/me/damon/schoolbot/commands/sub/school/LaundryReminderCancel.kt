package me.damon.schoolbot.commands.sub.school

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import kotlin.time.ExperimentalTime

class LaundryReminderCancel : SubCommand(
    name = "remind-cancel",
    category = CommandCategory.SCHOOL,
    description = "Cancels reminder",
)
{
    @OptIn(ExperimentalTime::class)
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val keys = event.schoolbot.taskHandler.tasks.keys
        val reminders = keys.filter { it.contains(event.user.id) }

        if (reminders.isEmpty()) return run {
            event.replyMessage("You have no active reminders")
        }

        val menu = SelectMenu("reminders:menu")
        { reminders.forEachIndexed { index, s -> option(s, index.toString()) } }

        event.sendMenuAndAwait(
            menu = menu,
            message = "Please select a reminder",
        ) {

        }
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val keys = schoolbot.taskHandler.tasks.keys
        val reminders = keys.filter { it.contains(event.user.id) }
            .map { Command.Choice(it, it) }

        event.replyChoices(reminders).queue()
    }
}