package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagintable
import net.dv8tion.jda.api.Permission
import kotlin.time.Duration

abstract class Command(
    override val name: String,
    override val category: CommandCategory,
    override val description: String,
    override val commandPrerequisites: String = "",
    override val coolDown: Long = 1000L,
    override val timeout: Duration = Duration.INFINITE,
    override val deferredEnabled: Boolean = true,
    override val memberPermissions: List<Permission> = listOf(),
    override val selfPermission: List<Permission> = listOf(),
    override val children: List<SubCommand> = listOf(),
    override val options: List<CommandOptionData<*>> = listOf()
) : AbstractCommand(), Pagintable
{
    suspend fun process(event: CommandEvent)
    {
        event.getSentOptions().forEach { i ->
            if (!i.validate(event.getOption(i.name)!!))
            {
                event.replyEmbed(
                    Embed {
                        title = "Validation failed on field ```${i.asOptionData().name}```"
                        description = "```${i.validationFailed}```"
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

    override fun getAsEmbed() = Embed {
        title = name
        field {
            name = "Description"

        }
    }

    abstract suspend fun onExecuteSuspend(event: CommandEvent)
}
