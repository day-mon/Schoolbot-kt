package me.damon.schoolbot

import dev.minn.jda.ktx.SLF4J
import dev.minn.jda.ktx.injectKTX
import me.damon.schoolbot.handler.CommandHandler
import me.damon.schoolbot.handler.ConfigHandler
import me.damon.schoolbot.handler.MessageHandler
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
import java.time.Instant
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

fun main()
{
    Schoolbot()
}

class Schoolbot : ListenerAdapter()
{

    private val logger by SLF4J

    /*
    val okhttp = OkHttpClient.Builder()
        .readTimeout()
            // The write timeout is applied for individual write IO operations. The default value is 10 secon
        .writeTimeout()
            // The connect timeout is applied when connecting a TCP socket to the target host. The default value is 10 seconds.
        .connectTimeout()

     */


    // handlers
    val startUpTime = Instant.now()
    val configHandler  = ConfigHandler()
    val messageHandler  = MessageHandler()

    // jda
    val jda = build()

    // after loading
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
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                    // doesn't cache members on start up
                .setChunkingFilter(ChunkingFilter.NONE)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .addEventListeners(
                    this,
                    SlashListener(this),
                    MessageListeners(this),
                    GuildListeners(),
                )
                .setActivity(Activity.playing("building...."))
                .injectKTX()
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