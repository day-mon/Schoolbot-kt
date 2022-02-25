package me.damon.schoolbot.objects.command

import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import kotlin.time.Duration

abstract class SubCommand(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String = "",
    override val coolDown: Long = 1000L,
    override val timeout: Duration = Duration.INFINITE,
    override val deferredEnabled: Boolean = true,
    override val memberPermissions: List<Permission> = listOf(),
    override val selfPermission: List<Permission> = listOf(),
    override val children: List<SubCommand> = listOf(),
    override val options: List<CommandOptionData<*>> = listOf(),
    val subCommandData: SubcommandData
        = SubcommandData(name, description)
        .addOptions(options.map { it.asOptionData() })

) : AbstractCommand()
{
    abstract suspend fun onExecuteSuspend(event: CommandEvent)
    open suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot) {}
}