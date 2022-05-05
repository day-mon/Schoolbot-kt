package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.SelectMenu
import dev.minn.jda.ktx.interactions.button
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.messages.reply_
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
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.models.CourseModel
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.objects.school.School
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.time.LocalDate

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
        val schoolService = event.getService<SchoolService>()
        val courseService = event.getService<CourseService>()

        val schoolName = event.getOption<String>("school_name")
        val pittSchools = schoolService.getPittSchoolsInGuild(event.guild.idLong) ?: return  event.replyErrorEmbed("Error has occurred while thing to get schools in `${event.guild.name}`")
        if (pittSchools.isEmpty()) return event.replyErrorEmbed("There are no pitt schools in ${event.guild.name}")

        val school: School = pittSchools.find { it.name == schoolName } ?: return event.replyErrorEmbed("$schoolName has not been found!")
        val terms = getThreeTerms()
        val selectionEvent = event.sendMenuAndAwait(
            SelectMenu("pittschool:menu") { terms.forEach { option(it.first, it.second) } },
            "Awesome we have selected `${school.name}`! Please select a term from the following term list!"
        ) ?: return

        val termNumber = selectionEvent.values[0]
        val term = terms.find { it.second == termNumber }?.first ?: return event.replyErrorEmbed("Error has occurred while thing to get term number $termNumber")

        val messageReceivedEvent = event.sendMessageAndAwait(
            message = "Nice! You selected `$term`! Please give me your class number"
        )
        val message = messageReceivedEvent.message.contentRaw

        event.replyMessageAndClear("Okay, looks good. I will now do the search for the class in the term: `$term` and with the number: `${message}`")
        val response = event.schoolbot.apiHandler.johnstownAPI.getCourse(termNumber, message)

        if (!response.isSuccessful)
        {
            event.replyErrorEmbed("Error has occurred while trying to get class")
            logger.error("Error has occurred while trying to get class", response.raw().asException())
        }

        val course = response.body() ?: run {
            event.replyErrorEmbed("Error occurred while attempting to get the response body")
            return logger.error("Body was null after retrieving it")
        }

        course.apply {
            course.term = term
        }

        val constraints = evaluateConstraints(course, event, term, courseService)
        if (constraints != String.empty)
        {
            event.replyErrorEmbed(tit = "An error has occurred while trying to add a class", error = constraints)
            return
        }

        logger.info("{}", school)


        val savedCourse = courseService.createPittCourse(event, school, course) ?: return event.replyErrorEmbed("An error has occurred. I will clean up any of the channels/roles I have created.")
        val embed = withContext(Dispatchers.IO) { savedCourse.getAsEmbed(event.guild) }

        // todo: fix thing here where it says interaction fails

        event.hook.editOriginal("Course created successfully!")
            .setEmbeds(embed)
            .queue()


        event.slashEvent.reply_("Would you like to add reminders for this course? (I will remind you **60**, **30**, **10** and **0 minutes** before class starts)?")
            .addActionRow(getActionRows(savedCourse, event, courseService))
            .queue()

    }

    private fun getActionRows(course: Course, event: CommandEvent, service: CourseService): List<Button>
    {
        val jda = event.jda
        val confirm = jda.button(label = "Confirm", style = ButtonStyle.SUCCESS, user = event.user) {
            it.deferReply().queue()
             val reminders = try { service.createReminders(course) } catch (e : Exception) { logger.error("Error occurred while creating reminders", e); return@button }

             if (reminders.isEmpty())
             {
                 event.replyErrorEmbed("Reminders were not created. Please try again")
                 return@button
             }

             event.replyMessageAndClear("${reminders.size} reminders have been created for ${course.name}!")
        }

        val exit = jda.button(label = "Exit", style = ButtonStyle.DANGER, user = event.user) {
            event.replyMessageAndClear("Alright. Have a nice day! ${Emoji.THUMB_UP.getAsChat()}")
        }

        return listOf(confirm, exit)

    }

    private suspend fun evaluateConstraints(course: CourseModel, event: CommandEvent, term: String, service: CourseService): String
    {
        val guild = event.guild
        val courseName = course.name
        return when
        {
            guild.roles.size == Constants.MAX_ROLE_COUNT -> "Cannot create role. `${guild.name}` is already at max role count"
            guild.textChannels.size == Constants.MAX_CHANNEL_COUNT -> "Cannot create channel. `${guild.name}` is already at max channel count"
            courseName.length >= 100 -> "${course.name} is longer than 100. Please add the class manually"
            service.findDuplicateCourse(
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