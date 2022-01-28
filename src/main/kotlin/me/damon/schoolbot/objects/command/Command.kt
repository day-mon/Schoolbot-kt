package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission

private val splitRegex = Regex(pattern = "\\.")

abstract class Command(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String = "",
    override val coolDown: Long = 1000L,
    override val memberPermissions: List<Permission> = listOf(),
    override val selfPermission: List<Permission> = listOf(),
    override val children: List<SubCommand> = listOf(),
) : AbstractCommand()
{
    suspend fun process(event: CommandEvent)
    {
        if (!event.hasSelfPermissions(selfPermission))
        {

        }
        else if (!event.hasMemberPermissions(memberPermissions))
        {

        }
        else
        {
            logger.info("${event.user.asTag} has executed $name")
            onExecuteSuspend(event)
        }
    }
    abstract suspend fun onExecuteSuspend(event: CommandEvent)
}
