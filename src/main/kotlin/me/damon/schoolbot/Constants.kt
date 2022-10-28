package me.damon.schoolbot

import de.jollyday.HolidayCalendar
import de.jollyday.HolidayManager
import de.jollyday.ManagerParameters
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

object Constants
{
    val DORMS = listOf(
        "willow",
        "hickory",
        "buckhorn",
        "llc",
        "oak",
        "maple",
        "heather",
        "hawthorn",
        "hemlock",
        "maple",
        "laurel",
        "larkspur",
        "cpas",
    )


    val AMERICAN_HOLIDAYS: HolidayManager = HolidayManager.getInstance(ManagerParameters.create(HolidayCalendar.UNITED_STATES))

    const val VERSION: String = "1.2.3"

    val TIMEZONES = mapOf(
        "Pacific Time - Los Angeles" to "America/Los_Angeles",
        "Mountain Time - Denver" to "America/Denver",
        "Central Time - Chicago" to "America/Chicago",
        "Eastern Time - New York" to "America/New_York",
        "Atlantic Time - Halifax" to "America/Halifax",
        "Brazil Time - Brasilia" to "America/Sao_Paulo",

        "Western Europe - London" to "Europe/London",
        "Central Europe - Berlin" to "Europe/Berlin",
        "Eastern Europe - Bucharest" to "Europe/Bucharest",
        "Russia - Moscow" to "Europe/Moscow",
        "Turkey - Istanbul" to "Turkey",

        "India - Kolkata" to "Asia/Kolkata",
        "Bangladesh - Dhaka" to "Asia/Dhaka",
        "Asia - Hong Kong" to "Asia/Hong_Kong",
        "Korea - Seoul" to "Asia/Seoul",
        "Japan - Tokyo" to "Asia/Tokyo",

        "Western Australia - Sydney" to "Australia/Sydney",
        "Northern Territory - Darwin" to "Australia/Darwin",
        "Queensland - Brisbane" to "Australia/Brisbane",
        "East Australia - Queensland" to "Australia/Queensland",
        "South Australia - Adelaide" to "Australia/Adelaide",
        "New Zealand - Auckland" to "Pacific/Auckland",

    )

    val JOKES = listOf(
        "I overdosed on Viagra once. The hardest day of my life.",
        "I am on a seafood diet. Every time I see food, I eat it.",
        "I'm no photographer, but I can picture us together.",
        "Why don't aliens visit our planet? Terrible ratings. One star.",
        "What do you have to do to have a party in space? You have to Planet.",
        "Not all math puns are terrible. Just sum.",
        "Why couldn't the leopard play hide and seek? Because he was always spotted.",
        "Why did the scientist install a knocker on his door? He wanted to win the No-bell prize!",
        "What was Forrest Gump's email password? \"1forrest1\"",
        "Did you hear about the man who jumped off a bridge in France? He was in Sein.",
        "Why do seagulls fly over the sea? Because if they flew over the bay they'd be bagels!",
        "I found a rock yesterday which measured 1760 yards in length. Must be some kind of milestone.",
        "Heard about the drug addict fisherman who accidentally caught a duck? Now he's hooked on the quack.",
        "Did you hear about that guy who fell into the infinity pool? Yeah... it took him forever to get out.",
        "What do you call a laughing motorcycle? A Yamahahaha.",
        "If you believe that the quickest way to a man's heart is the stomach, you know that you are aiming a little too high.",
        "Welcome to backhanded compliment club, it's so nice meeting people who don't care how they look.",
        "With great reflexes comes great response ability.",
        "What happens if you eat yeast and shoe polish? Every morning you will rise and shine!",
        "I relish the fact that you've mustard the strength to ketchup to me.",
        "For Halloween we dressed up as almonds. Everyone could tell we were nuts.",
        "I threw an Asian man down a flight of stairs. It was Wong on so many levels.",
        "What do you call a dictionary on drugs? HIGH-Definition.",
        "A teacher asks a student, \"Are you ignorant or just apathetic?\" The kid answers, \"I don't know and I don't care.\"",
        "I used to be a banker, but then I lost interest.",
        "Sign at the Urologist's office: URINE good hands.",
        "Justice is a dish best served cold because if it were served warm, it would be justwater.",
        "It's been raining for 3 days without stopping. My wife is in depression, she is standing and looking through the window. If the rain doesn't stop tomorrow, I'll have to let her in.",
        "Police have arrested the World tongue-twister Champion. I imagine he'll be given a tough sentence.",
        "I had a neck brace fitted years ago and I've never looked back since.",
        "I wanna make a joke about sodium, but Na..",
        "What do you call a cow with no legs? Ground beef.",
        "Did you hear about the monkeys who shared an Amazon account? They were prime mates.",
        "I hate peer pressure and you should too.",
        "A bus station is where a bus stops. A train station is where a train stops. On my desk, I have a work station..",
        "Got my girlfriend a \"get better soon\" card. She's not sick, I just think she could get better.",
        "My girlfriend tried to make me have sex on the hood of her Honda Civic. I refused. If I'm going to have sex, it's going to be on my own Accord.",
        "Did you hear about these new reversible jackets? I'm excited to see how they turn out.",
        "A huge thanks to the guy that just explained the word \"many\" to me. It means a lot.",
        "Last time I got caught stealing a calendar I got 12 months.",
        "Someone broke into my house last night and stole my Limbo stick. How low can you get?",
        "My psychiatrist said I was pre-occupied with the vengeance I told him \"oh yeah we'll see about that!\"",
        "I bought a box of condoms earlier today. The cashier asked if I'd like a bag. I said \"nah, I'll just turn the lights off.\"",
        "My boss says I intimidate the other employees, so I just stared at him until he apologized.",
        "Can a kangaroo jump higher than the Empire State Building? Of course. The Empire State Building can't jump.",
        "Why couldn't the bike stand up on it's own? It was two tired.",
        "My doctors office has two doctors on call at all times. Is that considered a pair a docs.",
        "You can't get on the same page with someone who has a different book.",
        "Did you hear about the Italian chef with a terminal illness? He pastaway.",
        "My grandfather tried to warn them about the Titanic. He screamed and shouted about the iceberg and how the ship was going to sink, but all they did was throw him out of the theater.",
        "What do you call people who are afraid of Santa Claus? Claustrophobic",
        "Having sex in an elevator is wrong on so many levels.",
        "The therapist asked my wife why she wanted to end our marriage. She said she hated all the constant Star Wars puns. I look at the therapist and said, \"Divorce is strong with this one!\"",
        "I finally realized my parents favored my twin brother. It hit me when they asked me to blow up balloons for his surprise birthday party.",
        "What do you call a priest that becomes a lawyer? A father in law.",
        "What do prisoners use to call each other? Cell phones.",
        "What did one eye say to the other eye? Between you and me something smells.",
        "People don't get my puns. They think they're funny.",
        "Did you hear about the depressed plumber? He's been going through some shit.",
        "I was born to be a pessimist. My blood type is B Negative.",
        "I know I know, smoking's bad for me and all. But, my mama told me never to be a quitter.",
        "When I was young, I always felt like a male trapped in a females body. Then I was born",
        "3 men are stranded in a boat with 4 cigarettes and no way to light them. So they toss the 4th cigarette overboard, which makes the whole boat a cigarette lighter.",
        "As a wizard, I enjoy turning objects into a glass. Just wanted to make that clear.",
        "I'm reading a horror story in Braille. Something bad is about to happen... I can feel it.",
        "What happened when the semicolon broke grammar laws? It was given two consecutive sentences.",
        "The first time I got a universal remote control, I thought to myself \"This changes everything\".",
        "How can you spot the blind guy at the nudist colony? It's not hard.",
        "My buddy set me up on a blind date & said, \"Heads up, she's expecting a baby.\" Felt like an idiot sitting in the bar wearing just a diaper.",
        "I hate insects puns, they really bug me.",
        "I always wanted to marry an Archeologist. The older I would get, the more interested she would become!",
        "I'm changing my name to 'Benefits' on Facebook. Next time someone adds me, It will say \"you are now friends with Benefits.\"",
        "Finally got around to watching Back To The Future... It's about time.",
        "My computer's got Miley Virus. It has stopped twerking.",
        "Cleaning mirrors is a job I could really see myself doing.",
        "Why did the bee get married? Because he found his honey.",
        "I own a pencil that used to be owned by William Shakespeare, but he chewed it a lot. Now I can't tell if it's 2B or not 2B.",
        "How do trees access the internet? They log in.",
        "Every time I tell a punny cow joke, I butcher it.",
        "I've decided to sell my Hoover... well, it was just collecting dust.",
        "A hole was found in the wall of a nudist camp. The police are looking into it.",
        "I was going to give him a nasty look, but he already had one.",
        "Did you know that if you hold your ear up to a strangers leg you can actually hear them say \"what the fuck are you doing?",
        "Today, my son asked \"Can I have a book mark?\" and I burst into tears. 11 years old and he still doesn't know my name is Brian.",
        "Q: What do you call a cow with a twitch? A: \"Beef Jerky!\"",
        "What do you call a cow during an earthquake? A milkshake.",
        "Three conspiracy theorists walk into a bar. You can't tell me that's just a coincidence!",
        "I am so poor I can't even pay attention.",
        "I got a part in a movie called \"Cocaine\". I only have one line.",
        "What do you call Watson when Sherlock isn't around? Holmeless.",
        "I just found an origami porn channel, but it is paper view only.",
        "It's hard to explain puns to kleptomaniacs because they always take things literally.",
        "What do you call an academically successful slice of bread? An honor roll.",
        "The best time to open a gift is the present.",
        "My IQ test results just came in and I'm really relieved. Thank God it's negative.",
        "Anyone who wanted to sell fish had to get permission from grandpa. He was known as the cod father.",
        "I'm trying to date a philosophy professor, but she doesn't even know if I exist or not.",
        "What is the best Christmas present ever? A broken drum - you can't beat it!",
        "Why did the cross-eyed teacher lose her job? Because she couldn't control her pupils.",
        "What do sea monsters eat for lunch? Fish and ships.",
        "Did you hear about the math teacher who's afraid of negative numbers? He will stop at nothing to avoid them.",
        "What musical instrument is found in the bathroom? A tuba toothpaste.",
        "I had a job tying sausages together, but I couldn't make ends meet.",
        "Isn't it scary that doctors call what they do \"practice\"?",
        "If Russians pronounce B's as V's then Soviet.",
        "Did you hear about the 2 silk worms in a race? It ended in a tie!",
        "Where do sick boats go to get healthy? To the dock!",
        "I have won first place in this Halloween costume contest 16 years in a row. This year I am dressed as a hotdog. I'm on a roll.",
        "I ran out of poker chips so used dry fruits for playing instead. People went nuts when they saw me raisin the stakes.",
        "Why doesn't the bike stand by itself? Because it's two tired.",
        "How does Moses make his tea? Hebrews it.",
    )

    val CURRENT_TIME = "<t:${Instant.now().epochSecond}>"
    val SPACE_REGEX = Regex("\\s+")
    val DEFAULT_LOCALE: Locale = Locale.US
    val DEFAULT_DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", DEFAULT_LOCALE)
    val DEFAULT_TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", DEFAULT_LOCALE)
    const val DEV_BOT_ID = 876829823618646036
    const val RED = 0x990f0f
    const val YELLOW = 0xf0e68c // khaki
    const val MAX_ROLE_COUNT = 250
    const val MAX_MENTIONABLE_LENGTH = 100
    const val MAX_CHANNEL_COUNT = 500
    const val SELECTION_MENU_MAX_SIZE = 25
    const val MAX_EMBED_TITLE_COUNT = 45
    val DEFAULT_CLIENT: OkHttpClient =
        OkHttpClient.Builder()
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60 , TimeUnit.SECONDS)
            .build()
}
