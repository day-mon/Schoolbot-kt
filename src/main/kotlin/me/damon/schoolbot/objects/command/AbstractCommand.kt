package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractCommand
{
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    abstract val name: String
    abstract val category: CommandCategory
    abstract val deferredEnabled: Boolean
    abstract val description: String
    abstract val commandPrerequisites: String
    abstract val coolDown: Long
    abstract val memberPermissions: List<Permission>
    abstract val selfPermission: List<Permission>
    abstract val children: List<SubCommand>
    abstract val options: List<OptionData>
    val commandData: CommandData
    get() = CommandData(name.lowercase(), description)
        .addOptions(options)
}
