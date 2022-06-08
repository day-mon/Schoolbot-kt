package me.damon.schoolbot.commands.sub.school.laundry

import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.option

import me.damon.schoolbot.ext.replyChoiceStringAndLimit
import me.damon.schoolbot.handler.TaskHandler
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import org.springframework.stereotype.Component

@Component
class LaundryReminderCancel(
    private val taskHandler: TaskHandler
) : SubCommand(
    name = "cancel",
    category = CommandCategory.SCHOOL,
    description = "Cancels reminder",
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val task = taskHandler.tasks
        val keys = task.keys
        val reminders = keys.filter { it.contains(event.user.id) }

        if (reminders.isEmpty()) return event.replyMessage("You have no active reminders")


        val menu = SelectMenu("reminders:menu")
        { reminders.forEachIndexed { _, s -> option(s.replace(event.user.id, "").replace("_", " "), s) } }


        val selectionEvent = event.awaitMenu(menu, "Please select a reminder") ?: return
        val option = selectionEvent.values.first()
        val canceled = taskHandler.cancelTask(option) ?: return event.replyMessage("Some how that reminder does not exist")


        if (!canceled) return event.replyMessageAndClear("Task cannot be cancelled. This could be because its completed already")


        task.remove(option)

        val choice = option.replace(event.user.id, "").replace("_", " ")
        event.replyMessageAndClear("$choice reminder has successfully been removed.")
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)

    {
        val keys = taskHandler.tasks.keys
        val reminders = keys.filter { it.contains(event.user.id) }
        event.replyChoiceStringAndLimit(reminders).queue()
    }
}