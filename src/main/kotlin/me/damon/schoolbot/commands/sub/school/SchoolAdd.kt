package me.damon.schoolbot.commands.sub.school

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.minn.jda.ktx.await
import dev.minn.jda.ktx.interactions.SelectionMenu
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.onSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.models.SchoolModel
import me.damon.schoolbot.objects.repository.SchoolRepository
import me.damon.schoolbot.web.asException
import me.damon.schoolbot.web.await
import me.damon.schoolbot.web.bodyAsString
import me.damon.schoolbot.web.get
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.time.ZoneId

class SchoolAdd : SubCommand (
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
                    if (models.size > 25) return@await run { event.replyMessage("Please attempt to narrow your search down. That search propagated ${models.size} results") }

                    val menu = SelectionMenu("school:menu") { models.forEachIndexed { index, schoolModel -> option(schoolModel.name, index.toString()) } }

                    val message = event.hook.editOriginal("Select an item from the menu to choose a school").setActionRow(menu).await()
                    val selectionEvent = event.jda.await<SelectionMenuEvent> { it.member!!.idLong == event.member.idLong &&  it.messageIdLong == message.idLong }
                    selectionEvent.deferReply().queue()
                    val school = models[selectionEvent.values[0].toInt()]

                    //TODO:  add spring impl to save

                }
                else -> {
                    logger.error("Unexpected error has occurred",  response.asException())
                    event.replyMessage("An unexpected error has occurred!")
                }

            }
        }
    }
}