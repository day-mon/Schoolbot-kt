@file:Suppress("unused")

package me.damon.schoolbot.commands.main.info

import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType

@Suppress("unused")
class About : Command(
    name = "About",
    category = CommandCategory.INFO,
    description = "Gives info on the user who called command",
    options = listOf(
        CommandOptionData<User>(OptionType.USER, "user", "Shows info about target user", false)
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val member = when
        {
            event.sentWithOption("user") ->
            {
                event.getOption("user")!!.asMember!!
            }
            else ->
            {
                event.member
            }
        }

        event.replyEmbed(
            generateEmbed(member)
        )

    }

    private fun generateEmbed(member: Member): MessageEmbed
    {
        // inline function looks hideous here
       return  EmbedBuilder()
            .setTitle("Information on **${member.user.asTag}**")
            .addField("Account creation date", "${member.timeCreated}", false)
            .addField("Join date", "${member.timeJoined}", false)
            .addField("Is Bot", "${member.user.isBot}", false)
            .addField("Boosting Since", "${member.timeBoosted ?: "Not boosting"}", false)
            .addField("User Id", member.id, false).addField("Roles", member.roles.joinToString { it.asMention }, false)
            // function call due to the fact that it.name return uppercase and underscores
            .addField("Permissions", member.permissions.joinToString { it.getName() }, false)
            .setThumbnail(member.user.avatarUrl)
           .build()
    }
}