package dartzee.theme

import java.awt.Color

object PrideColors {
    val red = Color.decode("#ED1C24")
    val orange = Color.decode("#FF7F27")
    val yellow = Color.decode("#FFF200")
    val lightGreen = Color.decode("#B5E61D")
    val darkGreen = Color.decode("#22B14C")
    val darkBlue = Color.decode("#00A2E8")
    val lightBlue = Color.decode("#99D9EA")
    val lightPurple = Color.decode("#B19CD7")
    val purple = Color.decode("#A349A4")
    val pink = Color.decode("#FFAEC9")
}

private val buttonOverrideColours =
    mapOf(
        "New Game" to PrideColors.red,
        "Players" to PrideColors.orange,
        "Leaderboards" to PrideColors.yellow,
        "Game Report" to PrideColors.darkGreen,
        "Sync Setup" to PrideColors.darkBlue,
        "Utilities" to PrideColors.lightBlue,
        "Dartzee" to PrideColors.pink,
        "Preferences" to PrideColors.purple,

        // Sync
        "Perform Sync" to PrideColors.darkBlue,
        "Push" to PrideColors.lightGreen,
        "Pull" to PrideColors.orange,
        "Reset" to Color.black,

        // dartzee rules
        "Add" to PrideColors.lightGreen,
        "Rename" to PrideColors.orange,
        "Copy" to PrideColors.lightBlue,
        "deleteTemplate" to PrideColors.pink,

        // Player management
        "STATS_X01" to PrideColors.darkGreen,
        "STATS_GOLF" to PrideColors.darkBlue,
        "STATS_ROUND_THE_CLOCK" to PrideColors.orange,
        "Achievements" to PrideColors.yellow,
        "Edit" to PrideColors.lightBlue,
        "Run simulation" to PrideColors.lightGreen,
        "Delete" to PrideColors.red,

        // Utilities
        "Delete Game" to PrideColors.red,
        "Create Backup" to PrideColors.orange,
        "Restore from backup" to PrideColors.yellow,
        "Perform database check" to PrideColors.lightGreen,
        "Thread stacks" to PrideColors.darkGreen,
        "Check for updates" to PrideColors.darkBlue,
        "View logs" to PrideColors.lightBlue,
        "Run achievement conversion" to PrideColors.pink,
        "Enter party mode" to PrideColors.purple,

        // Game setup
        "Launch game" to PrideColors.darkGreen,
        "Select" to PrideColors.lightGreen,
        "Unselect" to PrideColors.orange,

        // Navigation
        "Back" to PrideColors.red,
        "Next" to PrideColors.darkGreen,
    )

val Themes.PRIDE: Theme
    get() =
        Theme(
            ThemeId.Pride,
            "Beep boop.",
            PrideColors.red,
            PrideColors.purple,
            Color.decode("#FFFFC5"),
            fontColor = Color.black,
            lightBackground = PrideColors.pink,
            dartboardColours = PrideDartboardPainter(),
            svgWidthScaleFactor = 0.8f,
            menuFontSize = 15f,
            linkColour = PrideColors.darkBlue,
            buttonOverrideColours = buttonOverrideColours,
        )
