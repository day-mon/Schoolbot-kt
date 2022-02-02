package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagintable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.commands.OptionType

abstract class Command(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String = "",
    override val coolDown: Long = 1000L,
    override val deferredEnabled: Boolean = true,
    override val memberPermissions: List<Permission> = listOf(),
    override val selfPermission: List<Permission> = listOf(),
    override val children: List<SubCommand> = listOf(),
    override val options: List<CommandOptionData<*>> = listOf()
) : AbstractCommand(), Pagintable
{
    suspend fun process(event: CommandEvent)
    {
        for (i in event.getSentOptions())
        {
            if (!i.validate(event.getOption(i.name)!!))
            {
                event.replyEmbed(Embed {
                    title = "Not a valid option"
                    field {
                        name = "Issue"
                        value = "```${i.validationFailed}```"
                    }
                })
                return
            }
        }

        if (!event.hasSelfPermissions(selfPermission))
        {
            val correct = "I will need ${selfPermission.filter { it !in event.guild.selfMember.permissions}.joinToString { "`${it.getName()}`" } } permission(s) to run this command!"
            if (deferredEnabled) event.hook.editOriginal(correct).queue()
            else event.slashEvent.reply(correct).queue()
        }
        else if (!event.hasMemberPermissions(memberPermissions))
        {
            val correct = "You will need ${memberPermissions.filter { it !in event.member.permissions}.joinToString { "`${it.getName()}`" } } permission(s) to run this command!"
            if (deferredEnabled) event.hook.editOriginal(correct).queue()
            else event.slashEvent.reply(correct).queue()
        }
        else
        {
            logger.info("${event.user.asTag} has executed $name")
            onExecuteSuspend(event)
        }
    }

    override fun getAsEmbed(): MessageEmbed
    {
        TODO("Not yet implemented")
    }

    abstract suspend fun onExecuteSuspend(event: CommandEvent)
}
