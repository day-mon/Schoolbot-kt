package me.damon.schoolbot

import dev.minn.jda.ktx.SLF4J
import dev.minn.jda.ktx.injectKTX
import me.damon.schoolbot.handler.CommandHandler
import me.damon.schoolbot.handler.ConfigHandler
import me.damon.schoolbot.handler.MessageHandler
import me.damon.schoolbot.handler.TaskHandler
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
import okhttp3.OkHttpClient
import java.time.Instant
import java.util.List
import java.util.Random
import java.util.concurrent.TimeUnit
import javax.security.auth.login.LoginException
import kotlin.system.exitProcess

fun main()
{
    Schoolbot()
}

class Schoolbot : ListenerAdapter()
{

    /**
     * withContext(Dispatcher.Main) {
     *
     *      // this will run anything on the main thread
     * }
     *
     * withContext scope will run anything inside that block into whatever dispatcher its told to run into
     */
    private val logger by SLF4J
    private val okhttp = OkHttpClient.Builder()
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    // TIME SET BECAUSE SOME REQUEST TAKE A LONG TIME DEFAULT IS 10 SECONDS

    /*
    val okhttp = OkHttpClient.Builder()
        .readTimeout()
            // The write timeout is applied for individual write IO operations. The default value is 10 secon
        .writeTimeout()
            // The connect timeout is applied when connecting a TCP socket to the target host. The default value is 10 seconds.
        .connectTimeout()

     */


    // handlers
    val startUpTime = Instant.now()!!
    val configHandler  = ConfigHandler()
    val taskHandler = TaskHandler()
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
                .setHttpClient(okhttp)
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

        taskHandler.addRepeatingTask(
            name = "status_switcher",
            timeUnit = TimeUnit.SECONDS,
            duration = 30,
            block = {
                val random = Random()
                val activityList = listOf(
                    Activity.watching("mark sleep"),
                    Activity.streaming("warner growing", "https://www.youtube.com/watch?v=PLOPygVcaVE"),
                    Activity.watching("damon bench joesphs weight"),
                    Activity.streaming("chakra balancing seminar", "https://www.youtube.com/watch?v=vqklftk89Nw")
                )
                jda.presence.setPresence(OnlineStatus.ONLINE, activityList[random.nextInt(activityList.size)])
            }
        )
    }






}