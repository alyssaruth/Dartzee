package dartzee.preferences

import dartzee.utils.DartsColour.DARTBOARD_BLACK
import dartzee.utils.DartsColour.DARTBOARD_GREEN
import dartzee.utils.DartsColour.DARTBOARD_RED
import dartzee.utils.DartsColour.DARTBOARD_WHITE

object Preferences {
    val oddSingleColour = Preference("oddsing", DARTBOARD_WHITE)
    val oddDoubleColour = Preference("odddoub", DARTBOARD_GREEN)
    val oddTrebleColour = Preference("oddtreb", DARTBOARD_GREEN)
    val evenSingleColour = Preference("evensing", DARTBOARD_BLACK)
    val evenDoubleColour = Preference("evendoub", DARTBOARD_RED)
    val evenTrebleColour = Preference("eventreb", DARTBOARD_RED)

    val deviceId = Preference("deviceId", "")

    val aiAutoContinue = Preference("aiauto", true)
    val checkForUpdates = Preference("chkupd", true)
    val showAnimations = Preference("anim", true)

    val aiSpeed = Preference("aispd", 1000)
    val leaderboardSize = Preference("ldbrdsz", 50)

    val hueFactor = Preference("huefactor", 0.8)
    val fgBrightness = Preference("fgbri", 0.5)
    val bgBrightness = Preference("bgbri", 1.0)
}
