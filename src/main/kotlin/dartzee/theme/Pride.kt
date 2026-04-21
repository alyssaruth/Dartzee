package dartzee.theme

import java.awt.Color
import java.awt.Point
import java.awt.Rectangle

object PrideColors {
    val red: Color = Color.decode("#ED1C24")
    val orange: Color = Color.decode("#FF7F27")
    val yellow: Color = Color.decode("#FFF200")
    val lightGreen: Color = Color.decode("#B5E61D")
    val darkGreen: Color = Color.decode("#22B14C")
    val darkBlue: Color = Color.decode("#00A2E8")
    val lightBlue: Color = Color.decode("#99D9EA")
    val lightPurple: Color = Color.decode("#B19CD7")
    val purple: Color = Color.decode("#A349A4")
    val pink: Color = Color.decode("#FFAEC9")
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

private val humanFlags =
    listOf(
        "humanBlue.png",
        "humanGreen.png",
        "humanLightBlue.png",
        "humanLightGreen.png",
        "humanOrange.png",
        "humanPurple.png",
        "humanRed.png",
        "humanYellow.png",
    )

private fun randomHumanFlag() =
    Theme::class.java.getResource("/theme/pride/flags/${humanFlags.random()}")

private fun getBannerDetails(
    svgBounds: Rectangle,
    dartboardCenter: Point,
): List<BannerRenderDetails> {
    val center =
        Point(
            dartboardCenter.x + (0.02 * svgBounds.width).toInt(),
            dartboardCenter.y - (0.15 * svgBounds.height).toInt(),
        )
    val topBanner = BannerRenderDetails("Pride", (svgBounds.height * 0.45).toInt(), center)

    return listOf(topBanner)
}

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
            bannerTextRenderer = ::getBannerDetails,
            menuFontSize = 15f,
            buttonOverrideColours = buttonOverrideColours,
            customIcons = mapOf("/flags/humanFlag.png" to ::randomHumanFlag),
        )
