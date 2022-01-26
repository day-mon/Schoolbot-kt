package me.damon.schoolbot.objects.command

import net.dv8tion.jda.api.Permission

abstract class SubCommand(
    val parent: Command,
    syntax: String,
    name: String,
    description: String,
    memberPermissions: List<Permission> = emptyList(),
    selfPermission: List<Permission> = emptyList()
) : Command (
    name = name,
    syntax = syntax,
    description = description,
    calls = emptyList(),
    memberPermissions = memberPermissions,
    selfPermission = selfPermission
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {

    }
}