package me.damon.schoolbot.handler

import dev.minn.jda.ktx.util.SLF4J
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.reflections.Reflections
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component


private const val COMMANDS_PACKAGE = "me.damon.schoolbot.commands"
private val supervisor = SupervisorJob()
private val scope = CoroutineScope(Dispatchers.IO + supervisor)

@Component
class CommandHandler(private val context: ConfigurableApplicationContext)
{
    private val logger by SLF4J
    private val reflections = Reflections(COMMANDS_PACKAGE)
    val commands: MutableMap<String, Command> = mutableMapOf()


    fun registerCommands(jda: JDA)
    {
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
            commands[name] = instance
            commandsUpdate.addCommands(instance.commandData)
        }
        commandsUpdate.queue()
        logger.info("${commands.count()} have been loaded successfully")
        logger.debug("{}", commands.toList())
    }

    fun handle(event: SlashCommandInteractionEvent)
    {
        val schoolbot = context.getBean(Schoolbot::class.java)
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
        val schoolbot = context.getBean(Schoolbot::class.java)
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
