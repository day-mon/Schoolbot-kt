package me.damon.schoolbot.handler

import me.damon.schoolbot.objects.command.AbstractCommand
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.entities.Member
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap


@Component
class CooldownHandler
{
    private val cooledCommandMap: ConcurrentHashMap<String, CooledCommand> = ConcurrentHashMap<String, CooledCommand>()

    fun addCooldown(event: CommandEvent)
    {
        val command = event.command
        val key = "${event.user.idLong}_${command.id}"

        val time = System.currentTimeMillis() + command.coolDown.inWholeMilliseconds
        cooledCommandMap[key] = CooledCommand(event.command, event.member, time)
    }

    fun isOnCooldown(event: CommandEvent): Boolean
    {
        val key = "${event.user.idLong}_${event.command.id}"
        return cooledCommandMap.containsKey(key)
    }

    fun getCooldownTime(event: CommandEvent): Long
    {
        val command = event.command
        val key = "${event.user.idLong}_${command.id}"
        val cooledCommand = cooledCommandMap[key] ?: return 0
        return Duration.ofMillis(cooledCommand.time).toSeconds()
    }



    data class CooledCommand(val command: AbstractCommand, val member: Member, val time: Long)
}