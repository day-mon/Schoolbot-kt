package me.damon.schoolbot.commands.admin

import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.messages.reply_
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class Clear : Command(
    name = "Clear",
    category = CommandCategory.ADMIN,
    deferredEnabled = false,
    description = "Clears messages in the text channel that the command was executed in",
    selfPermission = listOf(Permission.MESSAGE_MANAGE),
    memberPermissions = listOf(Permission.ADMINISTRATOR),
)
{
    // TODO: Fix this command
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val slash = event.slashEvent
        val jda = event.jda

        val confirm = jda.button(label = "Confirm", style = ButtonStyle.SUCCESS, user = event.user) { button ->
            button.channel
                .iterableHistory
                .takeAsync(100)
                .thenAcceptAsync { list ->
                    val channel = button.channel
                    val messages = list.stream()
                        .filter { it.timeCreated.isBefore(OffsetDateTime.now().plusWeeks(2)) }
                        .toList()

                    channel.purgeMessages(messages)

                    channel.sendMessage("Successfully purged `${messages.size}` messages!").queue {
                        it.delete().queueAfter(5, TimeUnit.SECONDS, null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))
                    }
                }
        }


        val exit = jda.button(label = "Exit", style = ButtonStyle.DANGER, user = event.user) {
            it.reply("Operation was successfully cancelled").queue()
        }

        slash.reply_("You are about to delete 100 messages, click the checkmark to continue, click the X to cancel.")
            .addActionRow(confirm, exit)
            .queue()

    }
}