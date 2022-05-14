package me.damon.schoolbot.commands.main.admin

import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

class Prune : Command (
    name = "Prune",
    category = CommandCategory.ADMIN,
    deferredEnabled = false,
    description = "Clears all bot messages in the text channel command is called in",
    selfPermissions = listOf(Permission.MESSAGE_MANAGE),
    memberPermissions = listOf(Permission.ADMINISTRATOR))
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        event.slashEvent
            .reply_("This will clear the last 50 bot messages within the last two weeks in this channel. Would you like to proceed?")
            .addActionRows(getActionRows(event))
            .queue()
    }

    private fun getActionRows(event: CommandEvent): List<ActionRow>
    {
        val jda = event.jda
        val selfUser = jda.selfUser
        val confirm = jda.button(label = "Confirm", style = ButtonStyle.SUCCESS, user = event.user) { button ->
            button.channel.iterableHistory
                .takeAsync(100)
                .thenApplyAsync { list ->
                    val channel = button.channel
                    val messages = list.stream()
                        .filter { it.timeCreated.isBefore(OffsetDateTime.now().plusWeeks(2)) && selfUser.idLong == it.member?.idLong }
                        .limit(50)
                        .toList()
                    channel.purgeMessages(messages)
                    return@thenApplyAsync messages.size
                }
                .thenApplyAsync { size ->
                    button.channel.sendMessage("Successfully deleted `$size` messages")
                        .queueAfter(5, TimeUnit.SECONDS,
                            null,
                            ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))
                }
        }


        val exit = jda.button(label = "Exit", style = ButtonStyle.DANGER, user = event.user) {
            it.reply("Operation was successfully cancelled").queue()
        }
        return listOf(confirm, exit).into()
    }
}