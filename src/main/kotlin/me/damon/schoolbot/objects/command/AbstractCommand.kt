package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.Constants
import me.damon.schoolbot.bot.Schoolbot
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData
import java.util.*
import kotlin.time.Duration

abstract class AbstractCommand : Pagable
{
    val logger by SLF4J
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
            if (children.isNotEmpty()) s.addSubcommands(children.map { it.subCommandData })
            else s.addOptions(options.map { it.asOptionData() })
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

        if (!event.hasSelfPermissions(selfPermissions))
        {
            val correct = selfPermissions.filter { it !in event.guild.selfMember.permissions }.joinToString { "`${it.getName()}`" }
            sendMessage(event, embed = getPermissionErrorEmbed(correct))
        }
        else if (!event.hasMemberPermissions(memberPermissions))
        {
            val correct = memberPermissions.filter { it !in event.member.permissions }.joinToString { "`${it.getName()}`" }
            sendMessage(event, embed = getPermissionErrorEmbed(correct))
        }
        else if (category == CommandCategory.DEV)
        {
            if (event.user.id !in event.schoolbot.configHandler.config.developerIds)
                return sendMessage(event, "You must be a developer to run this command")

            logger.info("${event.user.asTag} has executed $name")
            onExecuteSuspend(event)
        }
        else
        {
            logger.info("${event.user.asTag} has executed $name")
            onExecuteSuspend(event)
        }
    }


    private fun sendMessage(e: CommandEvent, message: String =  String.empty, embed: MessageEmbed? = null)
    {
        if (deferredEnabled) e.hook.send(content = message, embed = embed).queue()
        else e.slashEvent.reply_(content = message, embed = embed).queue()
    }

    open suspend fun onExecuteSuspend(event: CommandEvent)
    {
        event.replyMessage("This command is not implemented yet.")
        throw NotImplementedError("Execution for $name has not implemented ")
    }

    open suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
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

