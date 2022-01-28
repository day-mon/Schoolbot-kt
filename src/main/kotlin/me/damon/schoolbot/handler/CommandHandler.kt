package me.damon.schoolbot.handler

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.SubCommand
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.reflections.Reflections
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import kotlin.concurrent.thread


private const val COMMANDS_PACKAGE = "me.damon.schoolbot.commands"
private val logger = LoggerFactory.getLogger(CommandHandler::class.java)
private val pool = Executors.newScheduledThreadPool(20) {
    thread(start = false, name = "Schoolbot Command-Thread", isDaemon = true, block = it::run)
}
private val supervisor = SupervisorJob()
private val scope = CoroutineScope(pool.asCoroutineDispatcher() + supervisor)


class CommandHandler(private val schoolbot: Schoolbot)
{
    private val reflections = Reflections(COMMANDS_PACKAGE)
    private val commands: Map<String, Command> = initCommands()

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

            if (instance is SubCommand)
            {
                continue
            }

            if (instance !is Command)
            {
                // not a command
                continue
            }

            val name = instance.name.lowercase()
            map[name] = instance
            commandsUpdate.addCommands(CommandData(name, instance.description))
        }
        commandsUpdate.queue()
        logger.info("${map.count()} have been loaded successfully")
        return Collections.unmodifiableMap(map)
    }

    fun hande(event: SlashCommandEvent)
    {
        val cmdName = event.name
        val command = commands[cmdName] ?: return
        scope.launch {
            command.process(
                CommandEvent(
                    schoolbot = schoolbot,
                    slashEvent = event,
                    command = command
                )
            )
        }
        logger.info("after block")
    }

}