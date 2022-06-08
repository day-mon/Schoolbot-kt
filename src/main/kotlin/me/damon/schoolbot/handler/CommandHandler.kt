package me.damon.schoolbot.handler

import dev.minn.jda.ktx.events.CoroutineEventListener
import dev.minn.jda.ktx.events.getDefaultScope
import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.launch
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.springframework.stereotype.Component

@Component
class CommandHandler(
    private val configHandler: ConfigHandler,
    commandList: List<Command>
) : CoroutineEventListener
{
    private val scope = getDefaultScope()
    private val logger by SLF4J
    private val commands: Map<String, Command> = commandList.associateBy { it.name.lowercase() }


    fun registerCommands(event: ReadyEvent)
    {
        val commandsUpdate = event.jda.updateCommands()
        commands.forEach {
          commandsUpdate.addCommands(it.value.commandData)
        }
        commandsUpdate.queue()
        logger.info("{} commands have been successfully registered", commands.size)
    }

    override suspend fun onEvent(event: GenericEvent)
    {
        when (event) {
            is SlashCommandInteractionEvent -> handleSlashCommand(event)
            is CommandAutoCompleteInteractionEvent -> handleAutoComplete(event)
            is ReadyEvent -> registerCommands(event)
        }
    }


    fun handleSlashCommand(event: SlashCommandInteractionEvent)
    {
        if (event.guild == null)
            return event.reply("This command must be sent from a guild").queue()

        val cmdName = event.name
        val group = event.subcommandGroup
        val subCommand = event.subcommandName
        val command = commands[cmdName]
            ?: return event.reply("$cmdName not found").queue()


        if (group != null) scope.launch {
            val sub = command.group[group]?.find { it.name ==  subCommand }
                ?: return@launch  event.reply("${command.name} $group $subCommand has not been found").queue()

            if (sub.deferredEnabled)
                event.deferReply().queue()

            sub.process(
                CommandEvent(command = sub, slashEvent = event), configHandler
            )

        }
        else if (subCommand != null) scope.launch {
            val sub = command.children.find { it.name == event.subcommandName }
                ?: return@launch event.reply("${command.name} $subCommand has not been found").queue()

            if (sub.deferredEnabled)
                event.deferReply().queue()

            sub.process(
                CommandEvent(command = sub, slashEvent = event), configHandler
            )

        }
        else scope.launch {
            if (command.deferredEnabled)
                event.deferReply().queue()

            command.process(
                CommandEvent(command = command, slashEvent = event), configHandler
            )
        }
    }

    fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent)
    {
        if (event.guild == null)
            return

        val command = event.name
        val group = event.subcommandGroup
        val sub = event.subcommandName
        val commandF = commands[command]
            ?: return logger.error("$command could not be found")

        if (group != null) scope.launch {
            val subC = commandF.group[group]?.find { it.name ==  sub }
                ?: return@launch logger.error("${commandF.name} $group $sub could not be found")
            subC.onAutoCompleteSuspend(event)
        }
        else if (sub != null)  scope.launch {
            val subCommand = commandF.children.find { it.name == event.subcommandName }
                ?: return@launch logger.error("${commandF.name} $sub could not be found")
            subCommand.onAutoCompleteSuspend(event)
        }
        else scope.launch {
            commandF.onAutoCompleteSuspend(event)
        }
    }


}
