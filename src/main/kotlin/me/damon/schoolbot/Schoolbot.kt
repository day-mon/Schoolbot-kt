package me.damon.schoolbot

import me.damon.schoolbot.handler.CommandHandler
import me.damon.schoolbot.handler.ConfigHandler
import me.damon.schoolbot.listener.GuildListeners
import me.damon.schoolbot.listener.MessageListeners
import me.damon.schoolbot.listener.SlashListener
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
import org.slf4j.LoggerFactory
import java.time.Instant
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

fun main()
{
    Schoolbot()
}

class Schoolbot : ListenerAdapter()
{

    private val logger = LoggerFactory.getLogger(Schoolbot::class.java)
    // handlers
    val configHandler  = ConfigHandler()

    // jda
    val jda = build()
    val startUpTime = Instant.now()
    val cmd = CommandHandler(this)

    private fun build(): JDA
    {
        try
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
            return JDABuilder.create(configHandler.config.token,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_MEMBERS
            )
                .disableCache(CacheFlag.ACTIVITY,
                    CacheFlag.VOICE_STATE,
                    CacheFlag.EMOTE,
                    CacheFlag.CLIENT_STATUS,
                    CacheFlag.ONLINE_STATUS
                )
                .addEventListeners(this)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                    // doesn't cache members on start up
                .setChunkingFilter(ChunkingFilter.NONE)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .addEventListeners(
                    SlashListener(this),
                    MessageListeners(this),
                    GuildListeners(),

                )
                .setActivity(Activity.playing("building...."))
                .build()
        }
        catch (e: LoginException)
        {
            logger.error("Login Exception has occurred", e)
            exitProcess(1)
        }
    }

    override fun onReady(event: ReadyEvent)
    {
        logger.info("Ready.")
        jda.presence.setPresence( OnlineStatus.ONLINE, Activity.watching("Your mom"))
    }






}