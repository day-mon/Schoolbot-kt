package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.SLF4J
import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands

abstract class AbstractCommand
{
    val logger by SLF4J
    abstract val name: String
    abstract val category: CommandCategory
    abstract val deferredEnabled: Boolean
    abstract val description: String
    abstract val commandPrerequisites: String
    abstract val coolDown: Long
    abstract val memberPermissions: List<Permission>
    abstract val selfPermission: List<Permission>
    abstract val children: List<SubCommand>
    abstract val options: List<CommandOptionData<*>>
    val commandData: CommandData
    get()
    {
        return when {
            children.isEmpty() ->
                Commands
                    .slash(
                        name.lowercase(),
                        description
                    )
                    .addOptions(
                        *options.map { it.asOptionData() }.toTypedArray()
                    )
            else ->
                Commands.slash(
                    name.lowercase(),
                    description
                ).addSubcommands(
                        children.map { it.subCommandData }
                    )
        }
    }


    suspend fun process(event: CommandEvent)
    {
        event.getSentOptions().forEach { i ->
            if (!i.validate(event.getOption(i.name)!!))
            {
                event.replyEmbed(
                    Embed {
                        title = "Validation failed on field ```${i.asOptionData().name}```"
                        description = "```${i.validationFailed}```"
                    })
                return
            }
        }

        if (!event.hasSelfPermissions(selfPermission))
        {
            val correct = "I will need ${selfPermission.filter { it !in event.guild.selfMember.permissions}.joinToString { "`${it.getName()}`" } } permission(s) to run this command!"
            sendMessage(event ,correct)
        }
        else if (!event.hasMemberPermissions(memberPermissions))
        {
            val correct = "You will need ${memberPermissions.filter { it !in event.member.permissions}.joinToString { "`${it.getName()}`" } } permission(s) to run this command!"
            sendMessage(event, correct)
        }
        else if (category == CommandCategory.DEV)
        {
            if (event.user.id !in event.schoolbot.configHandler.config.developerIds)
            {
                sendMessage(event, "You must be a developer to run this command")
                return
            }

            onExecuteSuspend(event)
        }
        else
        {
            logger.info("${event.user.asTag} has executed $name")
            onExecuteSuspend(event)
        }
    }


    private fun sendMessage(e: CommandEvent, message: String)
    {
        if (deferredEnabled) e.hook.editOriginal(message).queue()
        else e.slashEvent.reply(message).queue()
    }

     open suspend fun onExecuteSuspend(event: CommandEvent) {
        event.replyMessage("This command is not implemented yet.")
    }
    open suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot){}

}

