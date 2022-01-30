package me.damon.schoolbot.objects.models

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.objects.misc.Pagintable
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

data class LaundryModel(
    val applianceID: String,
    val type: String,
    val isWorking: Boolean,
    val timeRemaining: String,
    val isInUse: Boolean,
    val location: String
) : Pagintable
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
                value = if (isWorking) "Yes" else "No"
                inline = false
            }

            field {
                name = if (isInUse) "Time Remaining" else "In Use"
                value = if (isInUse) timeRemaining else "No"
                inline = false
            }


            //TODO: Fix color
            color = if(isWorking) Color.green.green else Color.red.red
        }

    }

}
