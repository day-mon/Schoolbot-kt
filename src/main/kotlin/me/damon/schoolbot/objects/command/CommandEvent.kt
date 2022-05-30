package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.interactions.components.replyPaginator
import dev.minn.jda.ktx.interactions.components.sendPaginator
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.bot.Schoolbot
import me.damon.schoolbot.ext.*
import me.damon.schoolbot.handler.ApiHandler
import me.damon.schoolbot.handler.CommandHandler
import me.damon.schoolbot.handler.ConfigHandler
import me.damon.schoolbot.handler.TaskHandler
import me.damon.schoolbot.objects.misc.Pagable
import me.damon.schoolbot.service.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class CommandEvent(
    val schoolbot: Schoolbot,
    val slashEvent: SlashCommandInteraction,
    val command: AbstractCommand,
)
{
    val logger by SLF4J
    val jda = slashEvent.jda
    val user = slashEvent.user
    val guild = slashEvent.guild!!
    val guildId = slashEvent.guild!!.idLong
    val member = slashEvent.member!!
    val hook = slashEvent.hook
    val service = getService<SchoolService>()
    val options: MutableList<OptionMapping> = slashEvent.options

    fun replyEmbed(embed: MessageEmbed, content: String = String.empty) = slashEvent.replyEmbed(embed, content)
    fun replyErrorEmbed(error: String, embedTitle: String = "Error has occurred") = slashEvent.replyErrorEmbed(
        errorString = error,
        title = embedTitle
    ).queue()

    fun replyAndEditWithDelay(message: String, delayMessage: String, duration: Duration) = when {
        slashEvent.isAcknowledged ->
            hook.editOriginal(message).queue {
               it.editMessage(delayMessage).queueAfter(duration)
            }
        else ->
            slashEvent.reply(message).queue {
                it.editOriginal(delayMessage).queueAfter(duration)
            }
    }
    fun send(message: String, actionRows: List<ActionRow> = emptyList(), embeds: List<MessageEmbed> = emptyList()) = slashEvent.send(content = message, actionRows = actionRows, embeds = embeds)

    fun replyMessage(message: String) = when  {
        slashEvent.isAcknowledged -> hook.sendMessage(message).queue()
        else -> slashEvent.reply(message).queue()
    }

    fun replyMessageAndClear(message: String) = when  {
        slashEvent.isAcknowledged -> hook.editOriginal(message).setActionRows(emptyList()).setEmbeds(emptyList()).queue()
        else -> slashEvent.reply(message).addActionRows(emptyList()).addActionRows(emptyList()).queue()
    }

    fun <T : Pagable> sendPaginator(embeds: Collection<T>) =
        sendPaginator(*embeds.map { it.getAsEmbed() }.toTypedArray())

    fun <T : Pagable> sendPaginatorColor(embeds: Collection<T>) =
        sendPaginator(*embeds.map { it.getAsEmbed(guild) }.toTypedArray())



    suspend fun awaitMenu(
        menu: SelectMenu,
        message: String,
        timeoutDuration: Duration = 1.minutes,
        acknowledge: Boolean = false,
        deleteAfter: Boolean = false,
        disableAfter: Boolean = false
    ) = slashEvent.awaitMenu(
        menu,
        message,
        timeoutDuration,
        acknowledge,
        deleteAfter,
        disableAfter
    )

    fun sendPaginator(vararg embeds: MessageEmbed)
    {
        if (embeds.isEmpty()) return replyErrorEmbed("There are no embeds to display")
        if (embeds.size == 1) return replyEmbed(embeds.first())

        if (slashEvent.isAcknowledged)
        {
            hook.sendPaginator(
                pages = embeds, expireAfter = 5.minutes
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()

        }
        else
        {
            slashEvent.replyPaginator(
                pages = embeds, expireAfter = 5.minutes
            ) {
                it.user.idLong == slashEvent.user.idLong
            }.queue()
        }
    }
    fun hasSelfPermissions(permissions: EnumSet<Permission>) = guild.selfMember.hasPermission(permissions)
    fun hasMemberPermissions(permissions: EnumSet<Permission>) = member.hasPermission(permissions)
    fun sentWithOption(option: String) = slashEvent.getOption(option) != null
    inline fun <reified T> getOption(name: String): T = when (T::class)
    {
        String::class -> slashEvent.getOption(name)?.asString as T
        // Could break if number is over 2.147 billion lol
        Int::class -> slashEvent.getOption(name)?.asLong?.toInt() as T
        Long::class -> slashEvent.getOption(name)?.asLong as T
        Double::class -> slashEvent.getOption(name)?.asDouble as T
        Boolean::class -> slashEvent.getOption(name)?.asBoolean as T
        Member::class -> slashEvent.getOption(name)?.asMember as T
        else -> throw IllegalArgumentException("Unknown type ${T::class}")
    }

    inline fun <reified T: SpringService> getService(): T = when (T::class)
    {
        GuildService::class -> schoolbot.guildService as T
        SchoolService::class -> schoolbot.schoolService as T
        ProfessorService::class -> schoolbot.professorService as T
        CourseService::class -> schoolbot.courseService as T
        AssignmentService::class -> schoolbot.assignmentService as T
        AssignmentReminderService::class -> schoolbot.assignmentReminderService as T
        else -> throw IllegalArgumentException("Unknown type ${T::class}")
    }

    inline fun <reified T> getHandler(): T = when (T::class) {
        CommandHandler::class -> schoolbot.commandHandler as T
        ApiHandler::class -> schoolbot.apiHandler as T
        TaskHandler::class -> schoolbot.taskHandler as T
        ConfigHandler::class -> schoolbot.configHandler as T
        else -> throw IllegalArgumentException("Unknown type ${T::class}")
    }

    fun getOption(option: String) = slashEvent.getOption(option)
    fun getSentOptions() =
        command.options.filter { commandOptionData -> commandOptionData.name in slashEvent.options.map { it.name } }

    fun sentWithAnyOptions() = slashEvent.options.isNotEmpty()


}