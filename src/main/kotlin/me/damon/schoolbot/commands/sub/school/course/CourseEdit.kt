package me.damon.schoolbot.commands.sub.school.course

import dev.minn.jda.ktx.interactions.components.Modal
import dev.minn.jda.ktx.interactions.components.SelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.edit
import dev.minn.jda.ktx.messages.into
import me.damon.schoolbot.Constants

import me.damon.schoolbot.ext.*
import me.damon.schoolbot.objects.command.CommandCategory
import me.damon.schoolbot.objects.command.CommandEvent
import me.damon.schoolbot.objects.command.CommandOptionData
import me.damon.schoolbot.objects.command.SubCommand
import me.damon.schoolbot.objects.school.Course
import me.damon.schoolbot.service.CourseService
import me.damon.schoolbot.service.SchoolService
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.springframework.stereotype.Component
import java.net.URI
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Component
class CourseEdit(
    private val schoolService: SchoolService,
    private val courseService: CourseService
) : SubCommand(
    name = "edit",
    category = CommandCategory.SCHOOL,
    description = "Edits a course given a school",
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
    )
)
{
    private val courseAttributes: MutableMap<String, String> = mutableMapOf()

    override suspend fun onExecuteSuspend(event: CommandEvent)
    {
        val id = event.getOption<String>("school_name").toUUID()
           ?: return event.replyErrorEmbed("Please use the auto complete feature to select a school. If you did and this happened.. I dont know how sorry, you're on your own")

        val school = try { schoolService.findSchoolById(id) }
        catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while trying to retrieve school") }
            ?: return event.replyErrorEmbed("School does not exist")


        val courses = try { courseService.findBySchool(school) }
        catch (e: Exception) { return event.replyErrorEmbed("Error has occurred while searching for courses")}

        val courseSelectionMenu = SelectMenu("course_edit") {
            courses.forEachIndexed { index, course -> option(course.name, index.toString()) }
        }

        val selectionMenuEvent = event.awaitMenu(
            menu = courseSelectionMenu,
            message = "Please select a school you wish to edit",
            deleteAfter = true
        ) ?: return


        val course = courses[selectionMenuEvent.values.first().toInt()]

        val informationEmbed = Embed {
            this.title = "Understanding editing a course"
            field(name = "Information", value = "Editing a course a bit more complicated because there are a lot of things going on. You will be presented with three buttons. A Section 1, Section 2, Confirm Changes, and an Exit buttons.", inline = false)
            field(name = "Buttons", value = "You will see 4 buttons. Section 1, Section 2, Confirm Changes and an Exit button. In Section 1 you will be allowed to edit the name, description, start date, end date, and days of the course.", inline = false)
            field(name = "Section One Information", value = "Some of these fields that you will edit when you click these buttons will have requirements: \n 1. Start Date must be before end date \n 2. End date must be after start date \n 3. The timespan between these days must not exceed 366 days \n 5. Days of the courses must be seperated by commas", true)
            field(name = "Section Two Information", value = "Just like above fields here will have requirements and they are as follows: \n 1. Start time must be before end time. \n 2. End must be after start time. \n 3. URL field must be an actual URL", true)
            field(inline = true)
            field(name = "Extras", value = "If you wish to change the role and channel of **${course.name}** you must get the role/channel id of the role/channel you can do this by right clicking on the role/channel and clicking copy id.", inline = false)
        }




       courseAttributes.putAll(
           "name" to course.name,
           "description" to course.description,
           "url" to course.url,
           "start_date" to course.startDate.atZone(school.zone).toLocalDate().format(Constants.DEFAULT_DATE_FORMAT),
           "start_time" to course.startDate.atZone(school.zone).toLocalTime().format(Constants.DEFAULT_TIME_FORMAT),
           "end_date" to course.endDate.atZone(school.zone).toLocalDate().format(Constants.DEFAULT_DATE_FORMAT),
           "end_time" to course.endDate.atZone(school.zone).toLocalTime().format(Constants.DEFAULT_TIME_FORMAT),
           "days" to course.meetingDays,
           "channel" to course.channelId.toString(),
           "role" to course.roleId.toString(),
       )


        selectionMenuEvent.replyEmbeds(informationEmbed)
            .setContent("Some information before we start")
            .addActionRow(acknowledgementButton(event, course))
            .queue()
    }


    private fun acknowledgementButton(event: CommandEvent, course: Course) = event.jda.button(
        style = ButtonStyle.SUCCESS,
        label = "I understand"
    ) {
        it.message.edit(components = emptyList()).queue()

        val buttons = listOf(
            sectionOneButton(event, course),
            sectionTwoButton(event, course),
            completeSave(event, course),
            exitProcess(event, course)
        )

        it.reply("Here are the edits as of now")
            .addActionRow(buttons)
            .addEmbeds(getCourseEmbed(course, event.jda))
            .queue()
    }

    private fun sectionOneButton(event: CommandEvent, course: Course): Button = event.jda.button (
        style = ButtonStyle.PRIMARY,
        user = event.user,
        label = "Section 1"
    ) { button ->
        val header = "Editing Section One of ${course.name}"
        val title = if (header.length > Constants.MAX_EMBED_TITLE_COUNT) "${header.substring(0..42)}..." else header

        val modal = Modal(
            id = "sectionOneCourseEdit",
            title = title
        ) {

            short(id = "name", label = "Name", value = course.name, requiredLength = 1..Constants.MAX_MENTIONABLE_LENGTH)
            paragraph(id = "description", label = "Description", value = courseAttributes["description"])
            short(id = "start_date", label = "Start date", value = courseAttributes["start_date"])
            short(id = "end_date", label = "End date", value = courseAttributes["end_date"])
            short(id = "days", label = "Meeting days", value = courseAttributes["days"])
        }
        val modalEvent = button.awaitModal(modal = modal, deferEdit = true, duration = 3.minutes) ?: return@button



        val name = modalEvent.getValue<String>("name") ?: return@button modalEvent.replyErrorEmbed("Name field must not be empty").queue()
        val description = modalEvent.getValue<String>("description") ?: return@button modalEvent.replyErrorEmbed("Description field must not be empty").queue()
        val startDate = modalEvent.getValue<LocalDate>("start_date")  ?: return@button modalEvent.replyErrorEmbed("Start date must be in MM/DD/YYYY Format").queue()
        val endDate = modalEvent.getValue<LocalDate>("end_date") ?: return@button modalEvent.replyErrorEmbed("End date must be in MM/DD/YYYY Format").queue()
        val days = modalEvent.getValue<String>("days") ?: return@button modalEvent.replyErrorEmbed("Days field must not be empty").queue()


        val daysValidated = validateDays(days)
        val validateDates = validateDates(startDate, endDate)
        if (validateDates != String.empty) return@button modalEvent.replyErrorEmbed(validateDates).queue()

        if (!daysValidated) return@button modalEvent.replyErrorEmbed("$days is not in the correct format. Each day of the week must be seperated by a comma").queue()


        courseAttributes["name"] = name
        courseAttributes["description"] = description
        courseAttributes["start_date"] = startDate.format(Constants.DEFAULT_DATE_FORMAT)
        courseAttributes["end_date"] = endDate.format(Constants.DEFAULT_DATE_FORMAT)
        courseAttributes["days"] = days.split(",").distinct().map { it.toTitleCase() }.joinToString { it }


        button.hook.editOriginalComponents(
            button.message.buttons
                .map {
                    if (it.id == button.button.id || it.isDisabled)
                        it.asDisabled()
                    else
                        it
                }.into()
        ).queue()

        button.hook.editOriginalEmbeds(getCourseEmbed(course, event.jda)).queue()
    }



    private fun sectionTwoButton(event: CommandEvent, course: Course): Button = event.jda.button (
        style = ButtonStyle.PRIMARY,
        user = event.user,
        label = "Section 2"
    ) { button ->
        val header = "Editing Section Two of ${course.name}"
        val title = if (header.length > Constants.MAX_EMBED_TITLE_COUNT) "${header.substring(0..42)}..." else header

        val modal = Modal(
            id = "sectionTwoCourseEdit",
            title = title
        ) {
            short(id = "start_time", label = "Start Time", value = courseAttributes["start_time"])
            short(id = "end_time", label = "End Time", value = courseAttributes["end_time"])
            short(id = "url", label = "URL", value = courseAttributes["url"])
            short(id = "channel", label = "Channel ID", value = courseAttributes["channel"])
            short(id = "role", label = "Role ID", value = courseAttributes["role"])
        }

        val modalEvent = button.awaitModal(modal = modal, deferEdit = true, duration = 3.minutes) ?: return@button


        val startTime = modalEvent.getValue<LocalTime>("start_time")
            ?: return@button modalEvent.replyErrorEmbed("The time provided is not valid time. Format hh:mm am/pm").queue()
        val endTime = modalEvent.getValue<LocalTime>("end_time")
            ?: return@button modalEvent.replyErrorEmbed("The time provided is not valid time. Format hh:mm am/pm").queue()
        val url = modalEvent.getValue<URI>("url")
            ?: return@button modalEvent.replyErrorEmbed("The url provided is not a valid url").queue()
        val channel = modalEvent.getValue<TextChannel>("channel")
            ?: return@button modalEvent.replyErrorEmbed("The channel provided is not a valid channel id in this guild").queue()
        val role = modalEvent.getValue<Role>("role")
            ?: return@button modalEvent.replyErrorEmbed("The role provided is not a valid role in this guild").queue()





        val timesValidated = validateTimes(startTime, endTime)
        if (timesValidated != String.empty) return@button modalEvent.replyErrorEmbed(timesValidated).queue()

        courseAttributes["start_time"] = startTime.format(Constants.DEFAULT_TIME_FORMAT)
        courseAttributes["end_time"] = endTime.format(Constants.DEFAULT_TIME_FORMAT)
        courseAttributes["url"] = url.toString()
        courseAttributes["channel"] = channel.id
        courseAttributes["role"] = role.id

        button.hook.editOriginalComponents(
            button.message.buttons
                .map {
                    if (it.id == button.button.id || it.isDisabled)
                        it.asDisabled()
                    else
                        it
                }.into()
        ).queue()

        button.hook.editOriginalEmbeds(getCourseEmbed(course, event.jda)).queue()
    }

    private fun validateTimes(startTime: LocalTime, endTime: LocalTime): String
    {
        if (!startTime.isBefore(endTime)) return "Start Time must be before end time"
        if (!endTime.isAfter(startTime)) return "End time must be after start time"
        val duration = Duration.between(startTime, endTime)
        if (duration.toHours() > 6) return "Class must not be larger then 6 hours"
        return String.empty
    }

    private fun validateDates(startDate: LocalDate, endDate: LocalDate): String
    {
        if (!startDate.isBefore(endDate)) return "Start Date must be before the End Date"
        if (!endDate.isAfter(startDate)) return "End Date must be after Start Date"
        // Reason I used period: https://bugs.openjdk.java.net/browse/JDK-8170275
        val timeBetween = Period.between(startDate, endDate)
        if (timeBetween.days > 366) return "Time between the start date and end date exceeded the limit. Limit: **366 Days**" // lol leap year meme
        return String.empty

    }

    private fun validateDays(daysStr: String): Boolean
    {
        val days = listOf("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")
        val trimmed = daysStr.trim()
        val splitDays = trimmed.split(",").map { it.trim().lowercase() }
        return splitDays.all { it in days }
    }




    private fun exitProcess(event: CommandEvent, course: Course) = event.jda.button(
        style = ButtonStyle.DANGER,
        label = "Exit"
    ) {
        val embed =  Embed {
            title = "Exiting..."
            description = "Exiting the editing to ${course.name}. I will delete this message in 15 seconds. If you wish you could edit this class again by calling the command"
            color = Constants.YELLOW
            timestamp = Instant.now()
        }
        it.message.edit(embeds = listOf(embed), components = emptyList())
            .queue { message -> message.delete().queueAfter(15.seconds) }
    }


    private fun completeSave(
        e: CommandEvent,
        course: Course
    ): Button = e.jda.button(
        style = ButtonStyle.SUCCESS,
        user = e.user,
        label = "Confirm Changes"
    ) { it ->

        val embed = Embed {
            this.title = "Making edits..."
            description =
                "I am currently making edits to ${course.name}, while we wait here's a joke **${Constants.JOKES.random()}**"
        }


        it.message.edit(
            components = emptyList(),
            embeds = listOf(embed),
        ).queue()
        val updateName = (course.name == courseAttributes.getOrDefault("name", course.name)).not()
        val startDateLd = LocalDateTime.parse(
            "${courseAttributes["start_date"]} ${courseAttributes["start_time"]}",
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Constants.DEFAULT_LOCALE)
        )
        val startDateOffset = course.school.zone.rules.getOffset(startDateLd)
        val startDate = startDateLd.toInstant(startDateOffset)

        val endDateLd = LocalDateTime.parse(
            "${courseAttributes["end_date"]} ${courseAttributes["end_time"]}",
            DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a", Constants.DEFAULT_LOCALE)
        )
        val endDateOffset = course.school.zone.rules.getOffset(endDateLd)
        val endDate =  endDateLd.toInstant(endDateOffset)

        val updateReminders = ((course.startDate == startDate).and(course.endDate == endDate)).not()
        val addReminders = (course.meetingDays == courseAttributes["days"]).not()
        val daysToAdd = (courseAttributes["days"]!!.split(",") - course.meetingDays.split(",")).map{ it.trim() }.map { dayStr -> DayOfWeek.valueOf(dayStr.uppercase()) }


        course.apply {
            this.name = courseAttributes.getOrDefault("name", course.name)
            this.description = courseAttributes.getOrDefault("description", course.description)
            this.url = courseAttributes.getOrDefault("url", course.url)
            this.channelId = courseAttributes.getOrDefault("channel", course.channelId.toString()).toLong()
            this.roleId = courseAttributes.getOrDefault("role", course.roleId.toString()).toLong()
            this.meetingDays = courseAttributes.getOrDefault("days", course.meetingDays)
            this.number = courseAttributes.getOrDefault("number", course.number.toString()).toLong()
            this.startDate = startDate
            this.endDate = endDate
        }


        val updatedCourse = try { courseService.update(course) }
        catch (e: Exception) { return@button it
                .replyErrorEmbed("Error has occurred while trying to update course. Please try again in a bit.")
                .queue() }


        it.message.edit(
            components = emptyList(),
            embeds = listOf(embed),
        ).queue()

        val guild = e.guild

        if (updateName) {
            guild.getRoleById(course.roleId)?.manager?.setName(updatedCourse.name)?.queue()
            guild.getTextChannelById(course.channelId)?.manager?.setName(updatedCourse.name)?.queue()
        }




        // Bug here where you can set course end before start
        // I think I figured it out. when getting the offset you want to get the offset at that current time and not now.
        // for example Instant.now() may produce an offset of -4 but Instant.now().plus(6.months) may produce an offset of -5 due to dls
        // fixed but ill keep this here for the culture
        if (updateReminders) {
            logger.debug("Updating Reminders for ${course.name}")
           try { courseService.refactorRemindersByCourse(course) }
           catch (e: Exception) { it.replyErrorEmbed("Error occurred while trying to recreate reminders.") }
        }

        if (addReminders) {
            logger.debug("Reminders needed to be created on days: {} for ${course.name}", daysToAdd)
            try { courseService.createReminderOnDay(daysToAdd, course) }
            catch (e: Exception) { it.replyErrorEmbed("Error occurred while trying to create new reminders") }
        }

        it.message.edit(
            content = "Update complete!",
            embeds = listOf(updatedCourse.getAsEmbed(guild))
        ).queue()

    }



    private fun getCourseEmbed(course: Course, jda: JDA) = Embed {
        this.title = "Changes of ${courseAttributes["name"]} at this time"
        this.url = course.url
        val zone = course.school.zone
        val date = course.startDate.atZone(zone)
        val desc = courseAttributes.getOrDefault("description", course.description)
        val description = if (desc.length >= MessageEmbed.DESCRIPTION_MAX_LENGTH) "${desc.substring(0, MessageEmbed.DESCRIPTION_MAX_LENGTH - 5)}..."
                          else desc

        val days = courseAttributes.getOrDefault(key = "days", defaultValue = course.meetingDays).split(",").map { it.toTitleCase() }.distinct()
        val role = courseAttributes.getOrDefault("role", course.roleId.toString()).toLong()
        val channel = courseAttributes.getOrDefault("channel", course.channelId.toString()).toLong()
        val startTime = "${courseAttributes.getOrDefault(key = "start_time", 
                            defaultValue = date.format(Constants.DEFAULT_TIME_FORMAT))} ${zone.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"
        val endTime = "${courseAttributes.getOrDefault(key = "end_time",
                            defaultValue = date.format(Constants.DEFAULT_TIME_FORMAT))} ${zone.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"

        field(name = "Name", value = courseAttributes.getOrDefault(key = "name", defaultValue = course.name), inline = true)
        field(name = "Number", value = courseAttributes.getOrDefault(key = "number", defaultValue = course.number.toString()), inline = true)
        field(inline = true)
        field(name = "Description", value = description, inline = false)
        field(name = "URL", value = courseAttributes.getOrDefault(key = "url", defaultValue = course.url), inline = false)
        field(name = "Days", value = days.joinToString { it }, inline = false)
        field(name = "Start Date", value = courseAttributes.getOrDefault(key = "start_date", defaultValue = date.format(Constants.DEFAULT_DATE_FORMAT)), inline = true )
        field(name = "Start Time", value = startTime, inline = true)
        field(inline = true)
        field(name = "End Date", value = courseAttributes.getOrDefault(key = "end_date", defaultValue = date.format(Constants.DEFAULT_DATE_FORMAT)), inline = true)
        field(name = "End Time", value = endTime, inline = true)
        field(inline = true)
        field(name = "Role", value = jda.getRoleById(role)?.asMention ?: "N/A", inline = true)
        field(name = "Channel", value = jda.getTextChannelById(channel)?.asMention ?: "N/A", inline = true)
        field(inline = true)
        color = jda.getRoleById(course.roleId)?.colorRaw ?: 0xfffff

    }



    override suspend fun onAutoCompleteSuspend(event: CommandAutoCompleteInteractionEvent)
    {
        val id = event.guild?.idLong ?: return logger.error("Error has occurred while fetching guild id in autocomplete")
        val pittSchools =  try { schoolService.getPittSchoolsInGuild(id) } catch (e: Exception)  { return }
        event.replyChoiceAndLimit(
            pittSchools.map { Command.Choice(it.name, it.id.toString()) }
        ).queue()
    }
}