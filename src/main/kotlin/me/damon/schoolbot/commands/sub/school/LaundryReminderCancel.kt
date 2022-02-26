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
        val tHandler = event.schoolbot.taskHandler
        val task = tHandler.tasks
        val keys = task.keys
        val reminders = keys.filter { it.contains(event.user.id) }

        if (reminders.isEmpty()) return run {
            event.replyMessage("You have no active reminders")
        }

        val menu = SelectMenu("reminders:menu")
        { reminders.forEachIndexed { _, s -> option(s.replace(event.user.id, "").replace("_", " "), s) } }

        event.sendMenuAndAwait(
            menu = menu,
            message = "Please select a reminder",
        ) {
            val option = it.values[0]
            val canceled = tHandler.cancelTask(option) ?: return@sendMenuAndAwait run {
                event.replyMessage("Some how that reminder does not exist")
            }

            if (!canceled) return@sendMenuAndAwait run {
                event.replyMessageAndClear("Task cannot be cancelled. This could be because its completed already")
            }

            task.remove(option)

            val choice = option.replace(event.user.id, "").replace("_", " ")
            event.replyMessageAndClear("$choice reminder has successfully been removed.")
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