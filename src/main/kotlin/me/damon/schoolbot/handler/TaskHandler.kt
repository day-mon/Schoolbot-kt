package me.damon.schoolbot.handler

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TaskHandler
{
    private val scheduler = Executors.newScheduledThreadPool(10)
    private val tasks = mutableMapOf<String, Future<*>>()

    fun addRepeatingTask(name: String, timeUnit: TimeUnit, duration: Long, block: () -> Unit): ScheduledFuture<*>
    {
       val job =  scheduler.scheduleAtFixedRate(
           /* command = */ block,
           /* initialDelay = */ 0,
           /* period = */ duration,
           /* unit = */ timeUnit
        )

        tasks[name] = job
        return job
    }

    fun startOnReadyTask(jda: JDA)
    {
        addRepeatingTask(
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

    fun cancelTask(name: String)
    = tasks[name]?.cancel(false)
}