package me.damon.schoolbot

import dev.minn.jda.ktx.SLF4J
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["src.main.kotlin.me.damon.schoolbot."])
@EnableCaching
open class SchoolbotApplication : CommandLineRunner
{
    val logger by SLF4J

    override fun run(vararg args: String?)
    {
        logger.info("Schoolbot is running!")
    }
}

fun main()
{
    runApplication<Schoolbot>()
}