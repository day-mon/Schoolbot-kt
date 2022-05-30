package me.damon.schoolbot

import me.damon.schoolbot.bot.Schoolbot
import me.damon.schoolbot.ext.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories("me.damon.schoolbot.objects.repository")
class Application : CommandLineRunner
{
    override fun run(vararg args: String?)
    {
        logger.info("nice")
    }
}

fun main() {
    runApplication<Schoolbot>()
}