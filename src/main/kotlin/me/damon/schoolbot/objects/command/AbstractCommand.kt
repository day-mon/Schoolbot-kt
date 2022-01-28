package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractCommand
{
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    abstract val name: String
    abstract val category: CommandCategory
    abstract val description: String
    abstract val commandPrerequisites: String
    abstract val coolDown: Long
    abstract val memberPermissions: List<Permission>
    abstract val selfPermission: List<Permission>
    abstract val children: List<SubCommand>
}
