package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission

abstract class Command(
    val name: String, // can't use reflection here
    val description: String,
    val syntax: String,
    val usageExample: String = "N/A",
    val commandPrerequisites: String = "[none]",

    val coolDown: Long = 1000L,

    val memberPermissions: List<Permission> = emptyList(),
    val selfPermission: List<Permission> = emptyList(),

    val children: List<Command> = emptyList(),
    val calls: List<String> = emptyList(),

    val parent: Command?,
    )
{

    open suspend fun onExecuteSuspend(event: CommandEvent)
    {
    }
}