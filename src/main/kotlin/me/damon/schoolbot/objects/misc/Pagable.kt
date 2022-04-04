package me.damon.schoolbot.objects.misc

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed

interface Pagable
{
    /**
     * Gets entity as a Message Embed
     * @return The Message Embed usually used for pagination
     */
    fun getAsEmbed(): MessageEmbed

    /**
     * Gets entity as a Message Embed
     * @return The Message Embed usually with color if not implemented.
     * Just calls [Pagable.getAsEmbed] with no args
     */
    fun getAsEmbed(guild: Guild): MessageEmbed = getAsEmbed()
}