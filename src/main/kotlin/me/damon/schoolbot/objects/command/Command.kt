package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission

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
    override val options: List<CommandOptionData<*>> = listOf()
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


        val validationFailed = event.getSentOptions().stream().map { it.validate }.anyMatch { it.equals(false) }

        if (validationFailed)
        {
            // yea yea fart guy
            event.replyMessage("Validation failed.. sorry");
            return
        }

        logger.info("${event.user.asTag} has executed $name")
        onExecuteSuspend(event)


    }

    abstract suspend fun onExecuteSuspend(event: CommandEvent)
}
