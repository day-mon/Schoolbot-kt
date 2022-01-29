package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.OptionData

abstract class SubCommand(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String,
    override val coolDown: Long,
    override val memberPermissions: List<Permission>,
    override val selfPermission: List<Permission>,
    override val children: List<SubCommand>,
    override val options: List<OptionData>
) : AbstractCommand()
{
    abstract suspend fun onExecuteSuspend(event: CommandEvent)
}