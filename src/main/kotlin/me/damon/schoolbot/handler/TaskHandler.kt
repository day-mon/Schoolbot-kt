package me.damon.schoolbot.handler

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

    fun cancelTask(name: String)
    = tasks[name]?.cancel(false)
}