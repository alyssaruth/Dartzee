package dartzee.preferences

import dartzee.utils.DartsColour.DARTBOARD_BLACK_STR
import dartzee.utils.DartsColour.DARTBOARD_GREEN_STR
import dartzee.utils.DartsColour.DARTBOARD_RED_STR
import dartzee.utils.DartsColour.DARTBOARD_WHITE_STR

object Preferences {
    val oddSingleColour = Preference("oddsing", DARTBOARD_WHITE_STR)
    val oddDoubleColour = Preference("odddoub", DARTBOARD_GREEN_STR)
    val oddTrebleColour = Preference("oddtreb", DARTBOARD_GREEN_STR)
    val eventSingleColour = Preference("evensing", DARTBOARD_BLACK_STR)
    val evenDoubleColour = Preference("evendoub", DARTBOARD_RED_STR)
    val evenTrebleColour = Preference("eventreb", DARTBOARD_RED_STR)

    val aiAutoContinue = Preference("aiauto", true)
    val checkForUpdates = Preference("chkupd", true)
    val showAnimations = Preference("anim", true)

    val aiSpeed = Preference("aispd", 1000)
    val leaderboardSize = Preference("ldbrdsz", 50)

    val hueFactor = Preference("huefactor", 0.8)
    val fgBrightness = Preference("fgbri", 0.5)
    val bgBrightness = Preference("bgbri", 1.0)
}
