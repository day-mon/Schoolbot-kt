@file:OptIn(ExperimentalTime::class)

package me.damon.schoolbot.commands.sub.school

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.messages.reply_
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.models.SchoolModel
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.web.asException
import me.damon.schoolbot.web.await
import me.damon.schoolbot.web.bodyAsString
import me.damon.schoolbot.web.get
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.time.ZoneId
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class SchoolAdd : SubCommand(
    name = "add",
    description = "Adds a school",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            type = OptionType.STRING,
            name = "school_name",
            description = "Name of the school you wish to add",
            isRequired = true
        )
    )
)
{
    private val apiUrl = "https://schoolapi.schoolbot.dev/search?name="
    private val backupApiUrl = "http://universities.hipolabs.com/search?name="

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {

        val schoolName = event.getOption("school_name")!!.asString
        val client = event.jda.httpClient
        val request = get(backupApiUrl + schoolName)


        event.replyMessage("Searching for `$schoolName`...")




        client.newCall(request).await(scope = event.scope) { response ->
            when
            {
                response.isSuccessful -> {
                    val json = response.bodyAsString() ?: return@await run {
                        event.replyMessage("Error has occurred while trying to get the response body")
                    }

                    logger.debug("Json Response: {}", json)

                    val om = jacksonObjectMapper()
                    val models: List<SchoolModel> = om.readValue(json)

                    if (models.isEmpty()) return@await run { event.replyMessage("There are no schools with the name `$schoolName`") }
                    if (models.size > 25) return@await run { event.replyMessage("Please attempt to narrow your search down. That search propagated `${models.size}` results") }

                    val menu = SelectMenu("school:menu") { models.forEachIndexed { index, schoolModel -> option(schoolModel.name, index.toString()) } }

                    event.sendMenuAndAwait(
                        menu = menu,
                        message = "Select an item from the menu to choose a school \n **Timeout is set to one minute**",
                        timeoutDuration = 1.minutes
                    ) {
                        val school = models[it.values[0].toInt()]
                        it.reply_("Does this look like the correct school?")
                            .addEmbeds(school.getAsEmbed())
                            .addActionRow(getActionRows(it, event, school.asSchool(ZoneId.systemDefault())))
                            .queue()
                    }
                }
                else -> {
                    logger.error("Unexpected error has occurred",  response.asException())
                    event.replyMessage("An unexpected error has occurred!")
                }

            }
        }
    }


    private fun getActionRows(event: SelectMenuInteractionEvent, cmdEvent: CommandEvent, school: School): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user, expiration = 1.minutes) {
              // cmdEvent.saveSchool(school)
        }

        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = event.user, expiration = 1.minutes) {
            event.hook.editOriginal("Aborting.. Thank you for using Schoolbot!")
                .setActionRows(Collections.emptyList())
                .setEmbeds(Collections.emptyList())
                .queue()
        }

        return listOf(yes, no)
    }
}