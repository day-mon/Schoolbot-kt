package me.damon.schoolbot.handler

import dev.minn.jda.ktx.util.SLF4J
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import org.springframework.stereotype.Component
import java.util.concurrent.*

@Component
class TaskHandler
{
    private val activityList = listOf(
        Activity.watching("mark sleep"),
        Activity.streaming("warner growing", "https://www.youtube.com/watch?v=PLOPygVcaVE"),
        Activity.watching("damon bench joesphs weight"),
        Activity.streaming("chakra balancing seminar", "https://www.youtube.com/watch?v=vqklftk89Nw")
    )
    private val scheduler = Executors.newScheduledThreadPool(10) { Thread(it, "Schoolbot TaskHandler-Thread") }
    private val logger by SLF4J
    val tasks = mutableMapOf<String, Future<*>>()



    fun addRepeatingTask(
        name: String,
        delay: Long = 0,
        timeUnit: TimeUnit,
        duration: Long,
        block: () -> Unit
    ): ScheduledFuture<*>
    {
        val job = scheduler.scheduleAtFixedRate(
            /* command = */ block,
            /* initialDelay = */ delay,
            /* period = */ duration,
            /* unit = */ timeUnit
        )
        tasks[name] = job
        return job
    }

    fun addTask(name: String, timeUnit: TimeUnit, duration: Long, block: () -> Unit)
    {
        val job = scheduler.schedule(
            block, duration, timeUnit
        )
        tasks[name] = job
        logger.info("Task with ID [{}] has been scheduled for {} {}(s)", name, duration, timeUnit.name.lowercase())

    }

    fun taskExist(name: String) = tasks.containsKey(name)


    fun startOnReadyTask(jda: JDA)
    {
        addRepeatingTask(
            name = "status_switcher",
            timeUnit = TimeUnit.MINUTES,
            duration = 5,
            block = {
            jda.presence.setPresence(OnlineStatus.ONLINE, activityList[ThreadLocalRandom.current()
                .nextInt(activityList.size)])
        })

        addRepeatingTask(
            name = "course_reminders",
            timeUnit = TimeUnit.SECONDS,
            duration = 10,
            block = {

        })


        addRepeatingTask(
            name = "assignment_reminders",
            timeUnit = TimeUnit.SECONDS,
            duration = 10,
            block = {

            })
    }

    fun cancelTask(name: String) = tasks[name]?.cancel(false)
}