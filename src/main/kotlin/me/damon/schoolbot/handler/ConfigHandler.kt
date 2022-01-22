package me.damon.schoolbot.handler

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.system.exitProcess


/*
    const = assigned at compile time (only primitives and strings)
    val = assigned at compile time
 */

private const val CONFIG_NAME = "schoolbot_cfg.json"

class ConfigHandler
{
    private val formatter = Json { prettyPrint = true; isLenient = true }
    private val logger = LoggerFactory.getLogger(ConfigHandler::class.java)
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
                    timeZone = "timezone",

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
            val config =  Json.decodeFromString<Config>(
                    File(CONFIG_NAME)
                        .readLines()
                        .joinToString(separator = "\n")
                )

            val timeZoneCheck = TimeZone.getTimeZone(config.timeZone)
                ?: {
                logger.info("{} is not a valid timezone. Please use a correct timezone", config.timeZone)
                exitProcess(1)
            }

            return config

        }
        catch (e: Exception)
        {
            logger.error("An error has occurred while attempting to decode json", e)
            exitProcess(1)
        }
    }


    @Serializable
    data class Config(
        val developerIds: List<String>,
        val token: String,
        val timeZone: String,
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