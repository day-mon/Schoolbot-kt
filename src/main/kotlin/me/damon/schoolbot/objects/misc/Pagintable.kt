package me.damon.schoolbot.objects.misc

import net.dv8tion.jda.api.entities.MessageEmbed

interface Pagintable
{
    fun getAsEmbed(): MessageEmbed
}