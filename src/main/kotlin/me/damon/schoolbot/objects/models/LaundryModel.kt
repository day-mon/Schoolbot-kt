package me.damon.schoolbot.objects.models

import dev.minn.jda.ktx.Embed
import me.damon.schoolbot.Constants
import me.damon.schoolbot.objects.misc.Pagable
import net.dv8tion.jda.api.entities.MessageEmbed

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

            footer {
                name = "Location $location"
            }


            color = if (!isWorking || timeRemaining == "Offline")  Constants.RED else  0x26a29
        }

    }

}
