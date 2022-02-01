package me.damon.schoolbot.commands.admin

import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.messages.reply_
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.Button
import net.dv8tion.jda.api.interactions.components.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit


class Clear : Command(name = "Clear",
    category = CommandCategory.ADMIN,
    deferredEnabled = false,
    description = "Clears messages in the text channel that the command was executed in",
    selfPermission = listOf(Permission.MESSAGE_MANAGE),
    memberPermissions = listOf(Permission.ADMINISTRATOR),
    options = listOf(CommandOptionData<Int>(
        type = OptionType.INTEGER,
        name = "amount_of_messages",
        description = "Amount of messages to clear",
        isRequired = false,
        validate = { it in 1..100 })))
{
    private val defaultClearAmount = 25L

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {

        val slash = event.slashEvent
        val sent = event.sentWithOption("amount_of_messages")
        val amount = if (sent) event.getOption("amount_of_messages")!!.asLong else defaultClearAmount

        if (amount > 100) return // add some validation somehow

        slash
            .reply_("You are about to delete `$amount` messages, click the checkmark to continue, click `Exit` to cancel.")
            .addActionRow(getActionRows(event, amount)).queue()
    }


    private fun getActionRows(event: CommandEvent, amount: Long): List<Button>
    {
        val jda = event.jda
        val confirm = jda.button(label = "Confirm", style = ButtonStyle.SUCCESS, user = event.user) { button ->
            button.channel.iterableHistory.takeAsync(amount.toInt() + 2).thenApplyAsync { list ->
                    val channel = button.channel
                    val messages =
                        list.stream().filter { it.timeCreated.isBefore(OffsetDateTime.now().plusWeeks(2)) }.toList()

                    val subList = messages.subList(2, messages.size)
                    println(subList.size)

                    channel.purgeMessages(subList)
                    return@thenApplyAsync subList.size
                }.thenAcceptAsync { size ->
                    button.channel.sendMessage("Successfully purged `${size}` messages!").queue {
                        it.delete()
                            .queueAfter(5, TimeUnit.SECONDS, null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE))
                    }
                }
        }


        val exit = jda.button(label = "Exit", style = ButtonStyle.DANGER, user = event.user) {
            it.reply("Operation was successfully cancelled").queue()
        }
        return listOf(confirm, exit)


    }
}