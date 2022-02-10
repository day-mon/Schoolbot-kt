package me.damon.schoolbot.handler

import dev.minn.jda.ktx.SLF4J
import kotlinx.coroutines.*
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.reflections.Reflections
import java.util.*
import kotlin.time.ExperimentalTime


private const val COMMANDS_PACKAGE = "me.damon.schoolbot.commands"
private val supervisor = SupervisorJob()
private val scope = CoroutineScope(Dispatchers.Default + supervisor)

// todo consider a thread pool??!

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

    @OptIn(ExperimentalTime::class)
    fun handle(event: SlashCommandInteractionEvent)
    {
        val cmdName = event.name
        val subCommand = event.subcommandName
        val command = commands[cmdName] ?: return

        if (command.deferredEnabled) event.deferReply().queue()

        if (subCommand != null)
        {
           val subC =  command.children
               .find { it.name == event.subcommandName }!!

                 scope.launch {
                     withTimeoutOrNull(command.timeout) {
                         subC.onExecuteSuspend(
                             CommandEvent(
                                 scope = scope, schoolbot = schoolbot, command = subC, slashEvent = event
                             )
                         )
                     }
                }
        }
        else
        {

                scope.launch {
                    withTimeoutOrNull(command.timeout) {
                        command.process(
                            CommandEvent(
                                schoolbot = schoolbot, slashEvent = event, command = command, scope = scope
                            )
                        )
                }
            }
        }
    }
}
