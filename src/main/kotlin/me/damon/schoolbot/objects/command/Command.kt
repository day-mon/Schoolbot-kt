package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.OptionData

abstract class Command(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String = "",
    override val coolDown: Long = 1000L,
    override val deferredEnabled: Boolean = true,
    override val memberPermissions: List<Permission> = listOf(),
    override val selfPermission: List<Permission> = listOf(),
    override val children: List<SubCommand> = listOf(),
    override val options: List<OptionData> = listOf()
) : AbstractCommand()
{
    suspend fun process(event: CommandEvent)
    {
        if (!event.hasSelfPermissions(selfPermission))
        {
            val correct = "I do not have the correct permissions to run this command"
            if (deferredEnabled) event.hook.editOriginal(correct).queue()
            else event.slashEvent.reply(correct).queue()
            // TODO: Tell user what is needed to run the command
            return
        }
        else if (!event.hasMemberPermissions(memberPermissions))
        {
            val correct = "You not have the correct permissions to run this command"
            if (deferredEnabled) event.hook.editOriginal(correct).queue()
            else event.slashEvent.reply(correct).queue()
        }
        else
        {
            logger.info("${event.user.asTag} has executed $name")
            onExecuteSuspend(event)
        }
    }
    abstract suspend fun onExecuteSuspend(event: CommandEvent)
}
