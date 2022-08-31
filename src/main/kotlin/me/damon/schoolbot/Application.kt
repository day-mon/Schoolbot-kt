package me.damon.schoolbot

import ch.qos.logback.classic.Level
import dev.minn.jda.ktx.events.CoroutineEventManager
import dev.minn.jda.ktx.jdabuilder.light
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.handler.CommandHandler
import me.damon.schoolbot.handler.ConfigHandler
import me.damon.schoolbot.handler.MessageHandler
import me.damon.schoolbot.handler.TaskHandler
import me.damon.schoolbot.listener.GuildListeners
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application(
    private val configHandler: ConfigHandler,
    private val commandHandler: CommandHandler,
    private val messageHandler: MessageHandler,
    private val guildListeners: GuildListeners,
    private val taskHandler: TaskHandler
) : CommandLineRunner
{

    private val logger by SLF4J
    override fun run(vararg args: String?)
    {
        val level = Level.valueOf(configHandler.config.logLevel.uppercase())
        (LoggerFactory.getLogger("ROOT") as ch.qos.logback.classic.Logger).level = level
        logger.info("Application has successfully built, Log Level set to: {}", configHandler.config.logLevel)
    }

    @Bean
    fun jda(): JDA = light(configHandler.config.token, enableCoroutines = true) {
        setEventManager(CoroutineEventManager())
        setEnabledIntents(GatewayIntent.MESSAGE_CONTENT)
        addEventListeners(commandHandler, messageHandler, guildListeners, taskHandler)
        setActivity(Activity.watching("1's turning to 0's and 0's turning to 1's"))
    }

}

fun main() {
    runApplication<Application>()
}