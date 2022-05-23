package me.damon.schoolbot.objects.misc

enum class Emoji(private val emote: String, private val unicode: String? = null, private val isAnimated: Boolean = false)
{
    ERROR("811388299310137354"),
    SUCCESS("811388330889052213"),
    THINKING(":thinking:", "\uD83E\uDD14"),
    BOOKS(":books: ", "\u1F4DA" ),
    PEAR(":pear:", "U+1F350"),
    WATERMELON(":watermelon:", "U+1F349"),
    PINEAPPLE(":pineapple:", "U+1F34D"),
    APPLE(":apple:", "U+1F34E"),
    BANANA(":banana:", "U+1F34C"),
    AVOCADO(":avocado:", "U+1F951"),
    EGGPLANT(":eggplant:", "U+1F346"),
    KIWI(":kiwi:", "U+1F95D"),
    GRAPES(":grapes:", "U+1F347"),
    BLUEBERRIES(":blueberries:", "U+1FAD0"),
    CHERRIES(":cherries:", "U+1F352"),
    ONION(":onion:", "U+1F9C5"),
    PEACH(":peach:", "U+1F351"),
    LEMON(":lemon:", "U+1F34B"),
    TANGERINE(":tangerine:", "U+1F34A"),
    MELON(":melon:", "U+1F348"),
    COCONUT(":coconut:", "U+1F965"),
    GARLIC(":garlic:", "U+1F9C4"),
    CUCUMBER(":cucumber:", "U+1F952"),
    SQUID(":squid:", "U+1F991"),
    THUMB_UP(":thumbsup:", "\uD83D\uDC4D"),
    THUMB_DOWN(":thumbsdown:", "\uD83D\uDC4E"),
    GREEN_TICK(":white_check_mark:", "\u2705"),
    GREEN_CROSS(":negative_squared_cross_mark:", "\u274E"),
    UPWARD_TREND(":chart_with_upwards_trend:", "\uD83D\uDCC8"),
    DOWNWARD_TREND(":chart_with_downwards_trend:", "\uD83D\uDCC9"),
    ARROW_LEFT(":arrow_left:", "\u2B05\uFE0F"),
    ARROW_RIGHT(":arrow_right:", "\u27A1\uFE0F"),
    STOP_SIGN(":octagonal_sign:", "\uD83D\uDED1"),
    FERRIS_WHEEL(":ferris_wheel:", "\uF3A1"),
    WASTE_BASKET(":wastebasket:", "\uD83D\uDDD1\uFE0F"),
    A(":a:", "\uF170" ),
    ZERO(":zero:", "\u0030\uFE0F"),
    ONE(":one:", "\u0031\uFE0F"),
    TWO(":two:", "\u0032\uFE0F"),
    THREE(":three:", "\u0033\uFE0F"),
    FOUR(":four:", "\u0034\uFE0F"),
    SMILEY(":smiley:", "\uD83D\uDE03"),
    FIVE(":five:", "\u0035\uFE0F"),
    SIX(":six:", "\u0036\uFE0F"),
    SEVEN(":seven:", "\u0037\uFE0F"),
    EIGHT(":eight:", "\u0038\uFE0F"),
    NINE(":nine:", "\u0039\uFE0F"),
    RED_CIRCLE(":red_circle:", "\uF534" ),
    BLUE_CIRCLE(":blue_circle:", "\uF535" ),
    TOOLS(":tools:", "\uF6E0"),
    ORANGE_CIRCLE(":orange_circle:", "\uF7E0" ),
    WHITE_CIRCLE(":white_circle:", "\u26AA" ),
    LAPTOP(":computer:", "\uF4BB"),
    PURPLE_CIRCLE(":purple_circle:", "\u26AA" ),
    BLACK_CIRCLE(":black_circle:", "\u26AB" ),
    WARNING(":warning:", "\u26A0");

    fun getAsChat(): String
    {
        return when (this.unicode)
        {
            null ->
            {
                if (this.isAnimated)
                {
                    "<a:emote:${this.emote}>"
                }
                else "<:emote:${this.emote}>"
            }
            else -> this.emote
        }
    }


    fun getAsReaction(): String
    {
        return this.unicode ?: ("emote:" + this.emote)
    }
}