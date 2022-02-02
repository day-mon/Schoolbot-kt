package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.SLF4J
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.CommandData

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
    get() = CommandData(name.lowercase(), description)
        .addOptions(*options.map { it.asOptionData() }.toTypedArray())
}

