package me.damon.schoolbot.commands.sub.school

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.objects.command.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.time.LocalDate

class CourseAddPitt : SubCommand(
    name = "pitt",
    description = "Adds a pitt class",
    category = CommandCategory.SCHOOL,
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "school_name",
            description = "Name of school Pitt School",
            autoCompleteEnabled = true,
            isRequired = true
        )
    )
)
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val schoolName = event.getOption("school_name")!!.asString
        val pittSchools = event.schoolbot.schoolService.getPittSchoolsInGuild(event.guild.idLong)
        if (pittSchools.isEmpty()) return run { event.replyErrorEmbed("There are no pitt schools in ${event.guild.name}") }

        val school = pittSchools.find { it.name == schoolName }?: return run { event.replyErrorEmbed("$schoolName has not been found!")}
        val terms = getThreeTerms()
        val selectionEvent =  event.sendMenuAndAwait(
            SelectMenu("pittschool:menu")
            { terms.forEach { option(it.first, it.second) } },
            "Awesome we have selected `${school.name}`! Please select a term from the following term list!"
        )

        val termNumber = selectionEvent.values[0]
        val term = terms.find { it.second == termNumber }!!.first

        val messageReceivedEvent = event.sendMessageAndAwait(
            message = "Nice! You selected `$term`! Please give me your class number"
        )
        val message = messageReceivedEvent.message.contentRaw

        event.replyMessageAndClear("Okay, looks good. I will now do the search for the class in the term: `$term` and with the number: `${message}`")
        val response = event.schoolbot.apiHandler.johnstownAPI.getCourse(termNumber, message)

        if ( !response.isSuccessful )
        {
            event.replyErrorEmbed("Error has occurred while trying to get class")
            logger.error("Error has occurred while trying to get class", response.raw().asException())
        }

        val course = response.body() ?: return run {
            event.replyErrorEmbed("Error occurred while attempting to get the response body")
            logger.error("Body was null after retrieving it")
        }

        event.replyMessage("${course.name} has been found!")

        // TODO: Finish Impl
    }

    private fun getThreeTerms(): MutableList<Pair<String, String>>
    {
        // https://www.registrar.pitt.edu/sites/default/files/pdf/PS%20Term%20Naming%20Convention.pdf
        // current term
        val list = mutableListOf<Pair<String, String>>()
        var term = currentTerm()
        val map = mapOf(
            4 to "Spring", 7 to "Summer", 1 to "Fall"
        )

        var date = LocalDate.now()
        var x = 0
        while (x <= 3)
        {
            for (i in term..7 step 3)
            {
                if (x >= 3) break
                val yr = date.year.toString().substring(4 - 2)
                val ending = "'$yr"

                list.add( "${map[i]} $ending" to "2${ending.removeRange(0..0)}$i")
                x = x.plus(1)
            }
            if (x >= 3) break
            term = 1
            date = date.plusYears(1)
        }
        return list
    }

    private fun currentTerm() = when (LocalDate.now().monthValue) {
        1, 2, 3, 4 -> 4
        5, 6, 7 -> 7
        8, 9, 10, 11, 12 -> 1
        else -> throw IllegalStateException("lol")
    }
    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val pittSchools = schoolbot.schoolService.getPittSchoolsInGuild(event.guild!!.idLong)
        event.replyChoices(pittSchools.mapIndexed { _, it -> CommandChoice(it.name).asCommandChoice() }).queue()

    }
}