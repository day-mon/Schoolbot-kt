package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.Permission

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
    override val options: List<CommandOptionData<*>> = listOf(),
             val group: Map<String, List<SubCommand>> = mapOf()
) : AbstractCommand(), Pagable
{

    override fun getAsEmbed() = Embed {
        title = name
        field {
            name = "Description"

        }
    }
}
