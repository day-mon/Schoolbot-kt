package me.damon.schoolbot.listener

import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MessageListeners(private val schoolbot: Schoolbot) : ListenerAdapter()
{
    override fun onMessageReceived(event: MessageReceivedEvent)
    {
        schoolbot.messageHandler.handle(event)
    }
}