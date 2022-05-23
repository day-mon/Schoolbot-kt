package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.editMessage_
import dev.minn.jda.ktx.messages.into
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.damon.schoolbot.Constants
import me.damon.schoolbot.Schoolbot
import me.damon.schoolbot.ext.*
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.misc.Emoji
import me.damon.schoolbot.objects.models.CourseModel
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.ErrorResponse
import java.time.LocalDate

class CourseAddPitt : SubCommand(
    name = "pitt",
    description = "Adds a pitt class",
    category = CommandCategory.SCHOOL,
    selfPermissions = enumSetOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES),
    memberPermissions = enumSetOf(Permission.MANAGE_CHANNEL, Permission.MANAGE_ROLES),
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

        val pittSchools = try { schoolService.getPittSchoolsInGuild(event.guild.idLong) }
            catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while thing to get schools in `${event.guild.name}`") }

        if (pittSchools.isEmpty()) return event.replyErrorEmbed("There are no pitt schools in ${event.guild.name}")

        val school = pittSchools.find { it.name == schoolName }
            ?: return event.replyErrorEmbed("$schoolName has not been found!")
        val terms = getThreeTerms()
        val selectionEvent = event.awaitMenu(
            SelectMenu("pittschool:menu") { terms.forEach { option(it.first, it.second) } },
            "Awesome we have selected `${school.name}`! Please select a term from the following term list!"
        ) ?: return

        val termNumber = selectionEvent.values.first()
        val term = terms.find { it.second == termNumber }!!.first

        val modal = Modal("course_number_modal", title = "Give me your class number that's in $term ") {
            short(id = "course_number", label = "Course Number" )
        }

        val modalEvent = selectionEvent.awaitModal(modal, deferReply = true) ?: return
        val courseNumberStr = modalEvent.getValue<String>("course_number") ?: return modalEvent.replyErrorEmbed("Course Number field must not be empty").queue()
        val courseNumber = courseNumberStr.toLongOrNull() ?: return modalEvent.replyErrorEmbed("$courseNumberStr is not a number").queue()

        val response = event.schoolbot.apiHandler.johnstownAPI.getCourse(termNumber, courseNumber)
        logger.debug("Response has been received from API. Request URL: {}", response.raw().request().url())

        if (!response.isSuccessful) run {
            if (response.code() == 503)
                return modalEvent.replyErrorEmbed("Pitt course site (Peoplesoft) is currently down for maintenance.").queue()
            modalEvent.replyErrorEmbed("Error has occurred while trying to get class").queue()
            return logger.error("Error has occurred while trying to get class", response.raw().asException())
        }

        val course = response.body() ?: run {
            modalEvent.replyErrorEmbed("Error occurred while attempting to get the response body")
            return logger.error("Body was null after retrieving it")
        }

        course.apply {
            course.term = term
        }

        val constraints = evaluateConstraints(course, event, term, courseService)
        if (constraints != String.empty)
            return modalEvent.replyErrorEmbed(constraints).queue()



        val savedCourse = try { courseService.createPittCourse(event, school, course) } catch (e: Exception) {
            logger.error("Error has occurred while trying to save course", e)
           return modalEvent.replyErrorEmbed("An error has occurred. I will clean up any of the channels/roles I have created.").queue()
        }


        val embed = withContext(Dispatchers.IO) { savedCourse.getAsEmbed(event.guild) }


        modalEvent.hook.editOriginal("Course created successfully! ")
            .setEmbeds(embed)
            .queue()

        modalEvent.hook.sendMessage("Would you like to add reminders for this course? (I will remind you **60**, **30**, **10** and **0 minutes** before class starts)")
            .addActionRows(getActionRows(savedCourse, event, courseService))
            .queue()
    }

    private fun getActionRows(course: Course, event: CommandEvent, service: CourseService): List<ActionRow>
    {
        val jda = event.jda
        val confirm = jda.button(label = "Confirm", style = ButtonStyle.SUCCESS, user = event.user) {

            it.message.edit(content = "Adding reminders... While we wait here's a joke. `${Constants.JOKES.random()}`", components = emptyList()).queue()

             try { service.createReminders(course) }
             catch (e : Exception)
             {
                 // to future me:  error handling is supposed to here
                 it.message.editMessage("Reminders were not created. Please try again")
                     .queue()
                 logger.error("Error occurred while creating reminders", e)
                 return@button
             }


             it.message.edit("Reminders have been created for `${course.name}`! Have a nice day ${Emoji.THUMB_UP.getAsChat()}").queue(null, ErrorHandler().ignore(ErrorResponse.UNKNOWN_INTERACTION))
        }

        val exit = jda.button(label = "Exit", style = ButtonStyle.DANGER, user = event.user) {
            it.editMessage_(components = emptyList(), content = "Okay have a nice day ${Emoji.THUMB_UP.getAsChat()}").queue()
        }

        return listOf(confirm, exit).into()

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
                number = course.classNumber.toLong(), termId = term
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
        val id = event.guild?.idLong ?: return logger.error("Error has occurred while fetching guild id in autocomplete")
        val pittSchools =  try { schoolbot.schoolService.getPittSchoolsInGuild(id) } catch (e: Exception)  { return }
        event.replyChoiceStringAndLimit(pittSchools.map { it.name }
            .filter { it.startsWith(event.focusedOption.value, ignoreCase = true) })
            .queue()

    }
}