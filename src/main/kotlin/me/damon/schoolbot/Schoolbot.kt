package me.damon.schoolbot


import dev.minn.jda.ktx.jdabuilder.injectKTX
import dev.minn.jda.ktx.util.SLF4J
import me.damon.schoolbot.handler.*
import me.damon.schoolbot.listener.GuildListeners
import me.damon.schoolbot.listener.MessageListeners
import me.damon.schoolbot.listener.SlashListener
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.GuildService
import me.damon.schoolbot.service.ProfessorService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess


fun main()
{
    SpringApplication.run(Schoolbot::class.java)
}

@EnableCaching
@SpringBootApplication
@Component
open class Schoolbot(
    val configHandler: ConfigHandler,
    val messageHandler: MessageHandler,
    val guildService: GuildService,
    val taskHandler: TaskHandler,
    val apiHandler: ApiHandler,
    val commandHandler: CommandHandler,

    val schoolService: SchoolService,
    val professorService: ProfessorService,
    val courseService: CourseService,

    private val guildListener: GuildListeners,
    private val messageListeners: MessageListeners,
    private val slashListener: SlashListener,
) : ListenerAdapter()
{
    private val logger by SLF4J

    @Bean
    open fun build(): JDA = try
    {

        /**
         * create        - creates empty jda builder with user defined settings (will only create empty if you dont pass in the token into create function)
         *               - will only cache self member
         * createLight   - creates jda builder with recommended default settings but disables all CacheFlags
         *               - will only cache members that are connected to voice, guild owner, and self member
         * createDefault - creates jda builder with recommended default settings (default settings may be subject to change)
         *               - caches all members
         *
         * Member cache is only relevant if you are accessing the member object outside an event
         */
        JDABuilder.create(
            configHandler.config.token,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_MESSAGE_REACTIONS,
            GatewayIntent.GUILD_MEMBERS
        ).disableCache(
            CacheFlag.ACTIVITY,
            CacheFlag.VOICE_STATE,
            CacheFlag.EMOTE,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.ONLINE_STATUS
        ).setMemberCachePolicy(MemberCachePolicy.NONE)
            // doesn't cache members on start up
            .setChunkingFilter(ChunkingFilter.NONE)
            .setStatus(OnlineStatus.DO_NOT_DISTURB)
            .addEventListeners(
                this,
                messageListeners,
                guildListener,
                slashListener
            )
            .setHttpClient(Constants.DEFAULT_CLIENT)
            .setActivity(Activity.playing("building...."))
            .injectKTX()
            .build()
    }
    catch (e: LoginException)
    {
        logger.error("Login Exception has occurred", e)
        exitProcess(1)
    }




    override fun onReady(event: ReadyEvent)
    {
        logger.info("Ready.")

        commandHandler.registerCommands(event.jda)
        taskHandler.startOnReadyTask(event.jda)
    }
}