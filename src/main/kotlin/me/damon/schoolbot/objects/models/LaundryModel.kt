package me.damon.schoolbot.objects.models

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

data class LaundryModel(
    val applianceID: String,
    val type: String,
    val isWorking: Boolean,
    val timeRemaining: String,
    val isInUse: Boolean,
    val location: String
) : Pagable
{
    override fun getAsEmbed(): MessageEmbed
    {
        return Embed {
            title = "Appliance ID [#${applianceID.replace("0", "")}]"


            field {
                name = "Appliance Type"
                value = type
                inline = false
            }

            field {
                name = "Working"
                value = when {
                    isWorking -> "Yes"
                    else -> "No"
                }
                inline = false
            }

            field {
                name = when {
                    isInUse -> "Time Remaining"
                    else -> "In Use"
                }
                value = when {
                    isInUse -> timeRemaining
                    else -> "No"
                }
                inline = false
            }


            color = if (isWorking) 0x26a29 /* green */ else 0x990f0f /* red */
        }

    }

}
