package me.damon.schoolbot.objects.command

import me.damon.schoolbot.ext.emptyEnumSet
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class SubCommand(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String = "",
    override val coolDown: Duration = 30.seconds,
    override val deferredEnabled: Boolean = true,
    override val memberPermissions: EnumSet<Permission> = emptyEnumSet(),
    override val selfPermissions: EnumSet<Permission> = emptyEnumSet(),
    override val children: List<SubCommand> = listOf(),
    override val options: List<CommandOptionData<*>> = listOf(),
    override val id: Int = "$name$children".hashCode(),
    val subCommandData: SubcommandData
        = SubcommandData(name, description)
        .addOptions(options.map { it.asOptionData() })
) : AbstractCommand()
