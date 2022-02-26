package me.damon.schoolbot.objects.command

import dev.minn.jda.ktx.SLF4J
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData


inline fun <reified T> CommandOptionData(type: Any, name: String, description: String, isRequired: Boolean = false, noinline validate: (T) -> Boolean = { true }, failedValidation: String = "", autoCompleteEnabled: Boolean = false, choices: List<CommandChoice> = listOf())
        = CommandOptionData(
                    type = T::class.java,
                    optionType = type,
                    name = name,
                    description = description,
                    autoCompleteEnabled = autoCompleteEnabled,
                    isRequired = isRequired,
                    validate = validate,
                    validationFailed = failedValidation,
                    choices = choices
                )



data class CommandOptionData<T>(
     val type: Class<T>,
     val optionType: Any,
     val name: String,
     val description: String,
     val autoCompleteEnabled: Boolean = false,
     val isRequired: Boolean = false,
     val validate: (T) -> Boolean = { true },
     val validationFailed: String,
     val choices: List<CommandChoice> = listOf(),

     )
{

    private val logger by SLF4J

    fun validate(mapping: OptionMapping): Boolean
    {
        val split = type.name.split(".")
        val size = split.size - 1

        return when (split[size]) {
            "Long" -> isValid(mapping.asLong)
            "Boolean" -> isValid(mapping.asBoolean)
            "User" -> isValid(mapping.asUser)
            "String" -> isValid(mapping.asString)
            "Role" -> isValid(mapping.asRole)
            "IMentionable" -> isValid(mapping.asMentionable)
            else ->
            {
                logger.warn("$name is not a valid mapping for ${mapping.type}")
                false
            }
        }
    }

    private fun isValid(any: Any) =  validate(type.cast(any))

    fun asOptionData(): OptionData = OptionData(/* type = */ optionType as OptionType, /* name = */
        name, /* description = */
        description, /* isRequired = */
        isRequired, /* isAutoComplete = */
        autoCompleteEnabled
    )
        .addChoices(choices.map { it.asCommandChoice() })


}


