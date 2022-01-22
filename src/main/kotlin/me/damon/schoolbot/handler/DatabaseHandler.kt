package me.damon.schoolbot.handler

import com.zaxxer.hikari.HikariDataSource
import me.damon.schoolbot.Schoolbot
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
import kotlin.system.exitProcess

class DatabaseHandler(private val schoolbot: Schoolbot)
{

    private val pool = initHikari()
    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun initHikari(): HikariDataSource
    {
        val hikari = HikariDataSource()
        val dbConfig = schoolbot.configHandler.config.databaseConfig

        hikari.poolName = "Schoolbot Database Pool"
        hikari.username = dbConfig.dbUser
        hikari.password = dbConfig.dbPassword
        hikari.driverClassName = dbConfig.dbDriver
        hikari.jdbcUrl = dbConfig.dbJdbcUrl


        /*
              The property controls the maximum size that the pool is allowed to reach, including both idle and in-use connections.
              Basically this valueBeingChanged will determine the maximum number of actual connections to the database backend.
              When the pool reaches this size, and no idle connections are available,
              calls to getConnection() will block for up to connectionTimeout milliseconds before timing out.
       */
        hikari.maximumPoolSize = 30
        /*
                 Set the maximum number of milliseconds that a client will wait for a connection from the pool.
                 If this time is exceeded without a connection becoming available, a SQLException will be thrown from DataSource.getConnection().
                 15 Seconds = 15000 ms
         */
        hikari.connectionTimeout = 10000
        /*
                The property controls the minimum number of idle connections that HikariCP tries to maintain in the pool, including both idle and in-use connections.
                If the idle connections dip below this valueBeingChanged, HikariCP will make a best effort to restore them quickly and efficiently.
        */
        hikari.minimumIdle = 10

        return try
        {
            HikariDataSource(hikari)
        }
        catch (e: Exception)
        {
            // logger
            exitProcess(1)
        }
    }


    private fun getConnection(): Connection?
    {
        return try
        {
            pool.connection
        }
        catch (e: SQLException)
        {
            logger.error("SQLException has occurred while trying to get a connection from the pool", e)
            null
        }
    }

    fun initTable(table: String)
    {

    }

    fun initTables()
    {

    }
}