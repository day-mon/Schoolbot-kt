package me.damon.schoolbot.listener

import me.damon.schoolbot.handler.MessageHandler
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.springframework.stereotype.Component

@Component
class MessageListeners(private val messageHandler: MessageHandler) : ListenerAdapter()
{
    override fun onMessageReceived(event: MessageReceivedEvent)
    {
        messageHandler.handle(event)
    }
}