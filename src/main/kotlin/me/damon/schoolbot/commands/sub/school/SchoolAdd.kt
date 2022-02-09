@file:OptIn(ExperimentalTime::class)

package me.damon.schoolbot.commands.sub.school

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.messages.reply_
import kotlinx.coroutines.withTimeoutOrNull
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.models.SchoolModel
import me.damon.schoolbot.web.asException
import me.damon.schoolbot.web.await
import me.damon.schoolbot.web.bodyAsString
import me.damon.schoolbot.web.get
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
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
    private val API_URL = "https://schoolapi.schoolbot.dev/search?name="
    private val BACKUP_API_URL = "http://universities.hipolabs.com/search?name="

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {

        val schoolName = event.getOption("school_name")!!.asString
        val client = event.jda.httpClient
        val request = get(API_URL + schoolName)


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

                    event.hook.editOriginal("Select an item from the menu to choose a school \n **Timeout is set to one minute**").setActionRow(menu).await()

                    withTimeoutOrNull(1.minutes) {
                        val selectionEvent =
                            event.jda.await<SelectMenuInteractionEvent> { it.member!!.idLong == event.member.idLong && it.componentId == menu.id }
                        val school = models[selectionEvent.values[0].toInt()]
                        selectionEvent.reply_("Does this look like the correct school?")
                            .addEmbeds(school.getAsEmbed())
                            .addActionRow(getActionRows(selectionEvent))
                            .queue()
                    } ?: event.hook.editOriginal("Command timed out. Please try again").setActionRows(Collections.emptyList()).queue()

                }
                else -> {
                    logger.error("Unexpected error has occurred",  response.asException())
                    event.replyMessage("An unexpected error has occurred!")
                }

            }
        }
    }


    private fun getActionRows(event: SelectMenuInteractionEvent): List<Button>
    {
        val jda = event.jda
        val yes = jda.button(label = "Yes", style = ButtonStyle.SUCCESS, user = event.user) { button ->
            // TODO: Add to database here
        }

        val no = jda.button(label = "No", style = ButtonStyle.DANGER, user = event.user) {
            event.hook.editOriginal("Aborting.. Thank you for using Schoolbot!")
                .setActionRows(Collections.emptyList())
                .setEmbeds(Collections.emptyList())
                .queue()
        }

        return listOf(yes, no)
    }
}