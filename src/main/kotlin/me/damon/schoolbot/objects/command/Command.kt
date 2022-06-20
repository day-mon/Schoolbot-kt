package me.damon.schoolbot.objects.command

import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.ext.emptyEnumSet
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class Command(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String = String.empty,
    override val coolDown: Duration = 30.seconds,
    override val deferredEnabled: Boolean = false,
    override val memberPermissions: EnumSet<Permission> = emptyEnumSet(),
    override val selfPermissions: EnumSet<Permission> = emptyEnumSet(),
    override val children: List<SubCommand> = listOf(),
    override val options: List<CommandOptionData<*>> = listOf(),
    override val id: Int = "$name$children".hashCode(),
    val group: Map<String, List<SubCommand>> = mapOf()
) : AbstractCommand()

