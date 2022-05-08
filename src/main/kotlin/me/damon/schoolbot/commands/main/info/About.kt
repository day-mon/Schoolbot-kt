@file:Suppress("unused")

package me.damon.schoolbot.commands.main.info

import dev.minn.jda.ktx.messages.Embed
import me.damon.schoolbot.ext.toDiscordTimeZone
import me.damon.schoolbot.objects.command.Command
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
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
                event.getOption<Member>("user")
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

    private fun generateEmbed(member: Member): MessageEmbed = Embed {
        title = "Information on **${member.user.asTag}**"
        field("Account creation date", member.timeCreated.toInstant().toDiscordTimeZone(), false)
        field("Join date", member.timeJoined.toInstant().toDiscordTimeZone(), false)
        field("Is Bot", "${member.user.isBot}", false)
        field("Boosting Since", "${member.timeBoosted ?: "Not boosting"}", false)
        field("User Id", member.id, false)
        field("Roles", member.roles.joinToString { it.asMention }, false)
        // function call due to the fact that it.name return uppercase and underscores
        field("Permissions", member.permissions.joinToString { it.getName() }, false)
        thumbnail = member.user.avatarUrl
    }
}