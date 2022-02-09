package me.damon.schoolbot.objects.command

import me.damon.schoolbot.objects.misc.Emoji

enum class CommandCategory(val catName: String, val emoji: Emoji)
{
    DEV("dev", Emoji.LAPTOP),
    ADMIN("admin", Emoji.A),
    FUN("fun", Emoji.FERRIS_WHEEL),
    INFO("info", Emoji.TOOLS),
    MISC("misc", Emoji.WHITE_CIRCLE),
    SCHOOL("school", Emoji.BOOKS),
    UNLABELED("unlabeled", Emoji.ERROR);



}