package me.damon.schoolbot.listener

import me.damon.schoolbot.Schoolbot
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MessageListeners(private val schoolbot: Schoolbot) : ListenerAdapter()
{
    override fun onMessageReceived(event: MessageReceivedEvent)
    {
        if (!event.isFromGuild) return
        val message = event.message
        val content = message.contentStripped
        if (content.isBlank()) return





    }

    override fun onMessageDelete(event: MessageDeleteEvent)
    {
        super.onMessageDelete(event)
    }

    override fun onMessageUpdate(event: MessageUpdateEvent)
    {
        super.onMessageUpdate(event)
    }


}