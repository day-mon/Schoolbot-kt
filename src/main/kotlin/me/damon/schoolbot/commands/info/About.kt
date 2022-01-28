package me.damon.schoolbot.commands.info

import jdk.jfr.Category
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import java.util.stream.Collectors

class About : Command(
    name = "About",
    category = CommandCategory.INFO,
    description = "Gives info on the user who called command"
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val member = event.member ?: return run {
            event.replyMessage("Member is null. Cannot continue")
        }


        event.replyEmbed(
            EmbedBuilder()
                .setTitle("Information on **${event.user.asTag}**")
                .addField("Account creation date", "${member.timeCreated}", false)
                .addField("Join date", "${member.timeJoined}", false)
                .addField("Boosting Since", "${member.timeBoosted ?: "Not boosting"}", false)
                .addField("User Id", member.id, false)
                .addField("Roles", event.member.roles.stream().map(Role::getAsMention).collect(Collectors.joining(", ")), false)
                .addField("Permissions", event.member.permissions.stream().map(Permission::getName).collect(Collectors.joining(", ")), false)
                .setThumbnail(event.user.avatarUrl)
                .build()
        )

        TODO("Fix stream use internal kotlin mapping")
    }
}