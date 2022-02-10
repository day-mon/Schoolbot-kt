package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.SLF4J
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import kotlin.time.Duration

abstract class AbstractCommand
{
    val logger by SLF4J
    abstract val name: String
    abstract val timeout: Duration
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
}

