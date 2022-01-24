package me.damon.schoolbot.cache

import me.damon.schoolbot.Schoolbot

class Cache(val schoolbot: Schoolbot)
{
    val messageCache: MessageCache = MessageCache()

}