package dartzee.theme

import dartzee.game.GameType
import dartzee.screen.animation.Animation
import dartzee.screen.animation.BadLuckTrigger
import dartzee.screen.animation.BruceyBonusTrigger
import dartzee.screen.animation.BustTrigger
import dartzee.screen.animation.CompositeAnimation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.time.LocalDate
import java.time.Month

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

    val all =
        listOf(
            red,
            orange,
            yellow,
            lightGreen,
            darkGreen,
            darkBlue,
            lightBlue,
            lightPurple,
            purple,
            pink,
        )

    fun forIndex(index: Int) = all[index % all.size]
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
    ) + (0..20).map { "DARTZEE_TILE_$it" to PrideColors.forIndex(it) }

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

private val bingpotAnimation =
    CompositeAnimation((1..2).map { Animation("bingpot$it", "/theme/pride/horrific/holt.png") })

private val bustAnimation =
    CompositeAnimation((1..3).map { Animation("buster$it", "/theme/pride/horrific/lily.png") })

private val ohMy =
    CompositeAnimation(
        (1..3).map { Animation("oh-my$it", "/theme/pride/horrific/george-takei.png") }
    )

private val prideAnimations: List<Pair<IAnimationTrigger, IAnimation>> =
    GameType.values().flatMap { gameType ->
        listOf(
            DartScoreTrigger(gameType, 50) to bingpotAnimation,
            DartScoreTrigger(gameType, 25) to bingpotAnimation,
            DartScoreTrigger(gameType, 0) to
                CompositeAnimation(
                    listOf(ohMy, Animation("nonsense", "/theme/pride/horrific/ncuti.png"))
                ),
        )
    } +
        listOf(
            BadLuckTrigger to Animation("hooray", "/theme/pride/horrific/todd.png"),
            BruceyBonusTrigger to Animation("betterEfforts", "/theme/pride/horrific/julian.png"),
            BustTrigger to bustAnimation,
        )

val Themes.PRIDE: Theme
    get() =
        Theme(
            ThemeId.Pride,
            "Is there enough rainbow in this theme d'you think?!",
            PrideColors.red,
            PrideColors.purple,
            Color.decode("#FFFFC5"),
            fontColor = Color.black,
            lightBackground = PrideColors.pink,
            dartboardColours = PrideDartboardPainter(),
            bannerTextRenderer = ::getBannerDetails,
            menuFontSize = 15f,
            buttonOverrideColours = buttonOverrideColours,
            festivalInfo = FestivalInfo(::findPride, "The next parade begins"),
            customIcons = mapOf("/flags/humanFlag.png" to ::randomHumanFlag),
            unlockDate = LocalDate.of(2026, Month.JUNE, 1),
            animations = prideAnimations.toMap(),
        )

private fun findPride(year: Int): Pair<LocalDate, LocalDate> =
    LocalDate.of(year, Month.JUNE, 1) to LocalDate.of(year, Month.JUNE, 30)
