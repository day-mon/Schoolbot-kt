package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.option
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Constants
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.asException
import me.damon.schoolbot.ext.empty
import me.damon.schoolbot.ext.replyChoiceStringAndLimit
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.models.CourseModel
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.time.LocalDate
import java.util.*

class CourseAddPitt : SubCommand(
    name = "pitt",
    description = "Adds a pitt class",
    category = CommandCategory.SCHOOL,
    selfPermission = listOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES),
    memberPermissions = listOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES),
    options = listOf(
        CommandOptionData<String>(
            optionType = OptionType.STRING,
            name = "school_name",
            description = "Name of school Pitt School",
            autoCompleteEnabled = true,
            isRequired = true
        )
    ),


    )
{
    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val service = event.schoolbot.schoolService
        val schoolName = event.getOption<String>("school_name")
        val pittSchools = service.getPittSchoolsInGuild(event.guild.idLong)
            ?: return run { event.replyErrorEmbed("Error has occurred while thing to get schools in `${event.guild.name}`") }
        if (pittSchools.isEmpty()) return run { event.replyErrorEmbed("There are no pitt schools in ${event.guild.name}") }

        val school = pittSchools.find { it.name == schoolName }
            ?: return run { event.replyErrorEmbed("$schoolName has not been found!") }
        val terms = getThreeTerms()
        val selectionEvent = event.sendMenuAndAwait(
            SelectMenu("pittschool:menu") { terms.forEach { option(it.first, it.second) } },
            "Awesome we have selected `${school.name}`! Please select a term from the following term list!"
        ) ?: return

        val termNumber = selectionEvent.values[0]
        val term = terms.find { it.second == termNumber }!!.first

        val messageReceivedEvent = event.sendMessageAndAwait(
            message = "Nice! You selected `$term`! Please give me your class number"
        ) ?: return
        val message = messageReceivedEvent.message.contentRaw

        event.replyMessageAndClear("Okay, looks good. I will now do the search for the class in the term: `$term` and with the number: `${message}`")
        val response = event.schoolbot.apiHandler.johnstownAPI.getCourse(termNumber, message)

        if (!response.isSuccessful)
        {
            event.replyErrorEmbed("Error has occurred while trying to get class")
            logger.error("Error has occurred while trying to get class", response.raw().asException())
        }

        val course = response.body() ?: return run {
            event.replyErrorEmbed("Error occurred while attempting to get the response body")
            logger.error("Body was null after retrieving it")
        }

        course.apply {
            course.term = term
            course.url = response.raw().request().url().toString()
        }

        val constraints = evaluateConstraints(course, event, term)
        if (constraints != String.empty) return run {
            event.replyErrorEmbed(
                tit = "An error has occurred while trying to add a class", error = constraints
            )
        }


        val savedCourse = event.schoolbot.schoolService.createPittCourse(event, school, course) ?: return run {
            event.replyErrorEmbed("An error has occurred. I will clean up any of the channels/roles I have created.")
        }


        val embed = withContext(Dispatchers.IO) { savedCourse.getAsEmbed(event.guild) }

        event.hook.editOriginal("Course created successfully").setEmbeds(embed).setActionRows(Collections.emptyList())
            .queue()


    }

    private suspend fun evaluateConstraints(course: CourseModel, event: CommandEvent, term: String): String
    {
        val guild = event.guild
        val courseName = course.name
        return when
        {
            guild.roles.size == Constants.MAX_ROLE_COUNT -> "Cannot create role. `${guild.name}` is already at max role count"
            guild.textChannels.size == Constants.MAX_CHANNEL_COUNT -> "Cannot create channel. `${guild.name}` is already at max channel count"
            courseName.length >= 100 -> "${course.name} is longer than 100. Please add the class manually"
            event.schoolbot.schoolService.findDuplicateCourse(
                name = courseName, number = course.classNumber.toLong(), termId = term
            ) != null -> "`${course.name} / ${course.classNumber}` already exist under term id `${term}`"
            else -> String.empty
        }
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

                list.add("${map[i]} $ending" to "2${ending.removeRange(0..0)}$i")
                x = x.plus(1)
            }
            if (x >= 3) break
            term = 1
            date = date.plusYears(1)
        }

        var test: Int
        for (xg in 1..10 step 3)
        {
            test = 3
            test.toString()
        }

        return list
    }

    private fun currentTerm() = when (LocalDate.now().monthValue)
    {
        1, 2, 3, 4 -> 4
        5, 6, 7 -> 7
        8, 9, 10, 11, 12 -> 1
        else -> throw IllegalStateException("lol")
    }

    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent, schoolbot: Schoolbot)
    {
        val pittSchools = schoolbot.schoolService.getPittSchoolsInGuild(event.guild!!.idLong) ?: return
        event.replyChoiceStringAndLimit(pittSchools.map { it.name }
            .filter { it.startsWith(event.focusedOption.value, ignoreCase = true) }).queue()

    }
}