package me.damon.schoolbot.handler

import dev.minn.jda.ktx.SLF4J
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.reflections.Reflections
import java.util.*


private const val COMMANDS_PACKAGE = "me.damon.schoolbot.commands"
private val supervisor = SupervisorJob()
private val scope = CoroutineScope(Dispatchers.IO + supervisor)


class CommandHandler(private val schoolbot: Schoolbot)
{
    private val logger by SLF4J
    private val reflections = Reflections(COMMANDS_PACKAGE)
    val commands: Map<String, Command> = initCommands()


    private fun initCommands(): MutableMap<String, Command>
    {
        val map = HashMap<String, Command>()
        val jda = schoolbot.jda
        val classes = reflections.getSubTypesOf(Command::class.java)
        val commandsUpdate = jda.updateCommands()


        for (cls in classes)
        {
            val constructors = cls.constructors

            if (constructors.isEmpty() || constructors[0].parameterCount > 0)
            {
                continue
            }

            val instance = constructors[0].newInstance()

            if (instance !is Command)
            {
                logger.warn("Non command found in the commands package {}", instance.javaClass.packageName)
                continue
            }

            val name = instance.name.lowercase()
            map[name] = instance
            commandsUpdate.addCommands(instance.commandData)
        }
        commandsUpdate.queue()
        logger.info("${map.count()} have been loaded successfully")
        logger.debug("{}", map.toList())
        return Collections.unmodifiableMap(map)
    }

    fun handle(event: SlashCommandInteractionEvent)
    {
        val cmdName = event.name
        val group = event.subcommandGroup
        val subCommand = event.subcommandName
        val command = commands[cmdName] ?: return

        if (command.deferredEnabled) event.deferReply().queue()

        if (group != null) scope.launch {
            val sub = command.group[group]!!.find { it.name ==  subCommand }!!
            sub.process(
                CommandEvent(scope = scope, schoolbot =  schoolbot, command = sub, slashEvent = event)
            )
        }
        else if (subCommand != null)  scope.launch {
            val sub = command.children.find { it.name == event.subcommandName }!!
            sub.process(
                CommandEvent(scope = scope, schoolbot =  schoolbot, command = sub, slashEvent = event)
            )
        }
        else scope.launch {
            command.process(
                CommandEvent(scope = scope, schoolbot =  schoolbot, command = command, slashEvent = event)
            )
        }
    }

    fun handleAutoComplete(event: CommandAutoCompleteInteractionEvent)
    {
        val command = event.name
        val group = event.subcommandGroup
        val sub = event.subcommandName
        val commandF = commands[command] ?: return

        if (group != null) scope.launch {
            val subC = commandF.group[group]!!.find { it.name ==  sub }!!
            subC.onAutoCompleteSuspend(event, schoolbot)
        }
        else if (sub != null)  scope.launch {
            val subCommand = commandF.children.find { it.name == event.subcommandName }!!
            subCommand.onAutoCompleteSuspend(event, schoolbot)
        }
        else scope.launch {
            commandF.onAutoCompleteSuspend(event, schoolbot)
        }
    }
}
