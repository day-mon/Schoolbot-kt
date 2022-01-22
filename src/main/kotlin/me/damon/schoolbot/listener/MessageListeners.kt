package me.damon.schoolbot.listener

import me.damon.schoolbot.cache.Cache
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MessageListeners(val cache: Cache) : ListenerAdapter()
{
    override fun onMessageReceived(event: MessageReceivedEvent)
    {
        if (!event.isFromGuild) return

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