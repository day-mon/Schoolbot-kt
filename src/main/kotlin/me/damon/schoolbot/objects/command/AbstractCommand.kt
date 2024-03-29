package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.SendDefaults
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.Constants
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.handler.ConfigHandler
import me.damon.schoolbot.handler.CooldownHandler
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Duration


@Component
abstract class AbstractCommand : Pagable
{
    val logger by SLF4J
    @Autowired
    lateinit var cooldownHandler: CooldownHandler
    @Autowired
    lateinit var configHandler: ConfigHandler
    abstract val id: Int
    abstract val name: String
    abstract val category: CommandCategory
    abstract val deferredEnabled: Boolean
    abstract val description: String
    abstract val commandPrerequisites: String
    abstract val coolDown: Duration
    abstract val memberPermissions: EnumSet<Permission>
    abstract val selfPermissions: EnumSet<Permission>
    abstract val children: List<SubCommand>
    abstract val options: List<CommandOptionData<*>>
    val commandData: CommandData
        get()
        {
            val s = Commands.slash(name.lowercase(), description)
            val command = this as Command
            if (command.group.isNotEmpty())
                s.addSubcommandGroups(
                    command.group.map {
                        SubcommandGroupData(it.key, "This ${it.key}'s")
                            .addSubcommands(it.value.map { cmd -> cmd.subCommandData })
                    })
            if (children.isNotEmpty())
                s.addSubcommands(children.map { it.subCommandData })
            else
                s.addOptions(options.map { it.asOptionData() })

            return s
        }

    suspend fun process(event: CommandEvent)
    {
        event.getSentOptions().forEach {
            if (!it.validate(event.getOption(it.name)!!))
            {
                return event.replyErrorEmbed(
                    embedTitle = "Validation failed on field ```${it.asOptionData().name}```",
                    error = "```${it.validationFailed}```"
                )
            }
        }

        when {
            !event.hasSelfPermissions(selfPermissions) ->
            {
                val correct = selfPermissions.filter { it !in event.guild.selfMember.permissions }.joinToString { "`${it.getName()}`" }
                sendMessage(event, embed = getPermissionErrorEmbed(correct))
            }
            event.user.id in configHandler.config.developerIds ->
            {
                logger.info("${event.user.asTag} has executed $name")
                onExecuteSuspend(event)
            }
            cooldownHandler.isOnCooldown(event) ->
            {
                val time = cooldownHandler.getCooldownTime(event)
                val cooldownEmbed = Embed {
                    title = "You are on cooldown!"
                    description = "The cooldown for this command is `${coolDown.inWholeSeconds}`s."
                    field(name = "Time Until Cooldown Over", value = "<t:$time:R>")
                    color = Constants.YELLOW
                }
                event.replyEmbed(cooldownEmbed)
            }
            !event.hasMemberPermissions(memberPermissions) ->
            {
                val correct = memberPermissions.filter { it !in event.member.permissions }.joinToString { "`${it.getName()}`" }
                sendMessage(event, embed = getPermissionErrorEmbed(correct))
            }
            category == CommandCategory.DEV ->
            {
                if (event.user.id !in configHandler.config.developerIds)
                    return sendMessage(event, "You must be a developer to run this command")

                logger.info("${event.user.asTag} has executed $name")
                onExecuteSuspend(event)
            }
            else ->
            {
                logger.info("${event.user.asTag} has executed $name")
                cooldownHandler.addCooldown(event)
                onExecuteSuspend(event)
            }
        }
    }

    private fun sendMessage(e: CommandEvent, message: String =  String.empty, embed: MessageEmbed? = null)
    {
        val embeds = if (embed == null) SendDefaults.embeds else listOf(embed)
        if (deferredEnabled) e.hook.send(content = message, embeds = embeds).queue()
        else e.slashEvent.reply_(content = message, embeds = embeds).queue()
    }

    suspend fun onExecuteSuspend(event: CommandEvent)
    {
        event.replyMessage("This command is not implemented yet.")
        throw NotImplementedError("Execution for $name has not implemented ")
    }

    suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)
    {
        throw NotImplementedError("Autocomplete for $name has not implemented ")
    }

    private fun getPermissionErrorEmbed(errors: String) = Embed {
        title = " ${Emoji.WARNING.getAsChat()} Permission Error"
        description = "I need some permissions to run this command!"
        field(name = "Permissions", value = errors)
        color = Constants.YELLOW
    }


    override fun getAsEmbed() = Embed {
        title = name
        field {
            name = "Description"
            value = this@AbstractCommand.description
        }

        field {
            name = "Category"
            value = this@AbstractCommand.category.name
        }
    }
}

