package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.SLF4J
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.replyPaginator
import dev.minn.jda.ktx.interactions.sendPaginator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import me.damon.schoolbot.Constants
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.ext.replyErrorEmbed
import me.damon.schoolbot.ext.toUUID
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.misc.Identifiable
import me.damon.schoolbot.objects.misc.Pagable
import me.damon.schoolbot.objects.school.School
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

class CommandEvent(
    val schoolbot: Schoolbot,
    val slashEvent: SlashCommandInteraction,
    val command: AbstractCommand,
    val scope: CoroutineScope,
)
{
    val logger by SLF4J
    val jda = slashEvent.jda
    val user = slashEvent.user
    val channel = slashEvent.channel
    val guild = slashEvent.guild!!
    val guildId = slashEvent.guild!!.idLong
    val member = slashEvent.member!!
    val hook = slashEvent.hook
    val service = schoolbot.schoolService

    val options: MutableList<OptionMapping> = slashEvent.options

    fun replyEmbed(embed: MessageEmbed, content: String = String.empty) = when
    {
        command.deferredEnabled -> hook.editOriginalEmbeds(embed).setActionRows(Collections.emptyList())
            .setContent(content).queue({ }) {
                logger.error(
                    "Error has occurred while attempting to send embeds for command ${command.name}", it
                )
                hook.editOriginal("Error has occurred while attempting to send embeds").queue()
            }
        else -> slashEvent.replyEmbeds(embed).setContent(content).queue({ }) {
            logger.error(
                "Error has occurred while attempting to send embeds for command ${command.name}", it
            )
            slashEvent.reply("Error has occurred while attempting to send embeds").queue()

        }
    }

    fun replyErrorEmbed(error: String, tit: String = "Error has occurred") = when
    {
        command.deferredEnabled -> hook.editOriginalEmbeds(Embed {
            title = tit
            description = error
            color = Constants.RED
        })
            .setContent(String.empty)
            .setActionRows(Collections.emptyList())
            .queue(null) { logger.error("Error has occurred while attempting to send embeds for command ${command.name}", it) }
        else -> slashEvent.replyEmbeds(Embed {
            title = tit
            description = error
            color = Constants.RED
        })
            .addActionRows(Collections.emptyList())
            .setContent("")
            .queue(null) { logger.error("Error has occurred while attempting to send embeds for command ${command.name}", it) }
    }

    fun replyAndEditWithDelay(message: String, delayMessage: String, unit: TimeUnit, time: Long)
    {
        // expression body looks meh
        if (command.deferredEnabled)
        {
            hook.editOriginal(message).queue {
                it.editMessage(delayMessage).queueAfter(time, unit)
            }
        }
        else
        {
            slashEvent.reply(message).queue {
                it.editOriginal(delayMessage).queueAfter(time, unit)
            }
        }
    }

    fun replyMessage(message: String) = when  {
        command.deferredEnabled -> hook.editOriginal(message).queue()
        else -> slashEvent.reply(message).queue()
    }

    fun replyMessageAndClear(message: String) = when  {
        command.deferredEnabled -> hook.editOriginal(message).setActionRows(Collections.emptyList()).setEmbeds(Collections.emptyList()).queue()
        else -> slashEvent.reply(message).addActionRows(Collections.emptyList()).addActionRows(Collections.emptyList()).queue()
    }

    fun replyMessageWithErrorEmbed(message: String, exception: Exception)
    {
        val embed = Embed {
            title = "Error occurred. Send this message to a developer if it constantly occurs"
            field {
                title = "Cause"
                value = exception.cause.toString()
                inline = true
            }
            description = """```kt
                            ${exception.stackTraceToString()}
                        ```""".trimIndent()
        }
        if (command.deferredEnabled)
        {
            hook.editOriginal(message)
                .setEmbeds(embed).queue()

        }
        else
        {
            slashEvent.reply(message)
                .addEmbeds(embed).queue()
        }
    }


    // This function must be inlined and reified so that the compiler can infer the type of the generic
    inline fun <reified T : Identifiable> findGenericByIdAndGet(optionName: String): T?
    {
        val genericStr = getOption<String>(optionName.trim())
        val id = genericStr.toUUID() ?: return run {
            replyErrorEmbed("Error occurred while trying to fetch school by id. ${Emoji.THINKING.getAsChat()}")
            null
        }
        return service.findGenericById(T::class.java, id) ?: return run {
            replyErrorEmbed("Error occurred while trying to fetch school by id. ${Emoji.THINKING.getAsChat()}")
            null
        }
    }

    fun <T : Pagable> sendPaginator(embeds: Collection<T>) =
        sendPaginator(*embeds.map { it.getAsEmbed() }.toTypedArray())

    fun <T : Pagable> sendPaginatorColor(embeds: Collection<T>) =
        sendPaginator(*embeds.map { it.getAsEmbed(guild) }.toTypedArray())




    /**
     * This function is used to send a paginator of embeds.
     * @param embeds The embeds to send
     * @param timeoutDuration The duration in seconds before the paginator times out
     * @param acknowledge whether to acknowledge the message
     * @return The interaction event
     *
     */
    suspend fun sendMenuAndAwait(
        menu: SelectMenu, message: String, timeoutDuration: Long = 1, acknowledge: Boolean = false
    ) = withTimeoutOrNull(timeoutDuration * 1) {
        hook.editOriginal("$message | Time out is set to $timeoutDuration seconds").setActionRow(menu).queue()
        jda
            .await<SelectMenuInteractionEvent> { it.member!!.idLong == member.idLong && it.channel.idLong == slashEvent.channel.idLong }
            .also { if (acknowledge) it.deferEdit().queue() }
    } ?: run {
        replyErrorEmbed("Command has timed out try again please")
        null
    }

    /**
     * This function is used to send a paginator of embeds.
     * @param embeds The embeds to send
     * @param timeoutDuration The duration in seconds before the paginator times out
     * @param acknowledge whether to acknowledge the message
     * @return The interaction event
     *
     */
    suspend fun sendMessageAndAwait(
        message: String,
        rows: List<ActionRow> = Collections.emptyList(),
        timeoutDuration: Long = 1
    ): MessageReceivedEvent? = withTimeoutOrNull(timeoutDuration * 60000) {
        hook.editOriginal(message).setActionRows(rows).queue()
        jda.await { it.member!!.idLong == member.idLong && it.channel.idLong == slashEvent.channel.idLong }
    } ?: run {
        hook.replyErrorEmbed(body = "Command has timed out try again please")
        null
    }


    fun sendPaginator(vararg embeds: MessageEmbed)
    {
        if (embeds.isEmpty()) return run { replyErrorEmbed("There are no embeds to display") }
        if (embeds.size == 1) return run { replyEmbed(embeds[0]) }

        if (command.deferredEnabled)
        {


            hook.sendPaginator(
                pages = embeds, expireAfter = Duration.parse("5m")
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()

        }
        else
        {
            slashEvent.replyPaginator(
                pages = embeds, expireAfter = Duration.parse("5m")
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()
        }
    }
    fun hasSelfPermissions(permissions: List<Permission>) = guild.selfMember.hasPermission(permissions)
    fun hasMemberPermissions(permissions: List<Permission>) = member.hasPermission(permissions)
    fun sentWithOption(option: String) = slashEvent.getOption(option) != null
    inline fun <reified T> getOption(name: String): T = when (T::class)
    {
        String::class -> slashEvent.getOption(name)?.asString as T
        // Could break if number is over 2.147 billion lol
        Int::class -> slashEvent.getOption(name)?.asLong?.toInt() as T
        Long::class -> slashEvent.getOption(name)?.asLong as T
        Double::class -> slashEvent.getOption(name)?.asDouble as T
        Boolean::class -> slashEvent.getOption(name)?.asBoolean as T
        else -> throw IllegalArgumentException("Unknown type ${T::class}")
    }

    fun getOption(option: String) = slashEvent.getOption(option)
    fun getSentOptions() =
        command.options.filter { commandOptionData -> commandOptionData.name in slashEvent.options.map { it.name } }

    fun sentWithAnyOptions() = slashEvent.options.isNotEmpty()


}