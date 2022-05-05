package me.damon.schoolbot.handler

import dev.minn.jda.ktx.SLF4J
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import java.io.File
import kotlin.system.exitProcess


/*
    const = assigned at compile time (only primitives and strings)
    val = assigned at compile time
 */

private const val CONFIG_NAME = "schoolbot_cfg.json"
@Component("configHandler")
class ConfigHandler
{
    private val formatter = Json { prettyPrint = true; isLenient = true }
    private val logger by SLF4J
    val config = initConfig()

    private fun initConfig(): Config
    {
        val configFile = File(CONFIG_NAME)

        if (!configFile.exists())
        {
            configFile.createNewFile()


            val defaultValues = formatter.encodeToString(
                Config(
                    token = "token",
                    developerIds = listOf("-1".repeat(3)),

                    databaseConfig = DatabaseConfig(
                        dbUser = "",
                        dbPassword = "",
                        dbDriver = "",
                        dbHostName = "",
                        dbJdbcUrl = ""
                    )
                )
            )
            configFile.writeText(defaultValues)
        }
        return loadConfig()
    }


    private fun loadConfig(): Config
    {
        try
        {
            return Json.decodeFromString(
                File(CONFIG_NAME)
                    .readLines()
                    .joinToString(separator = "\n")
            )

        } catch (e: Exception)
        {
            logger.error("An error has occurred while attempting to decode json", e)
            exitProcess(1)
        }
    }


    @Serializable
    data class Config(
        val developerIds: List<String>,
        val token: String,
        val databaseConfig: DatabaseConfig
    )


    @Serializable
    data class DatabaseConfig(
        val dbUser: String,
        val dbHostName: String,
        val dbPassword: String,
        val dbJdbcUrl: String,
        val dbDriver: String,
    )
}