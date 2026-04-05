package dartzee.theme

import dartzee.game.GameType
import dartzee.`object`.ColourWrapper
import dartzee.screen.animation.Animation
import dartzee.screen.animation.CompositeAnimation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import java.awt.Color
import java.time.LocalDate
import java.time.Month

private val witchAnimation =
    CompositeAnimation((1..3).map { Animation("witch$it", "/theme/halloween/horrific/witch.png") })

private val halloweenAnimations: List<Pair<IAnimationTrigger, IAnimation>> =
    GameType.values().flatMap { gameType ->
        listOf(
            DartScoreTrigger(gameType, 0) to witchAnimation,
            DartScoreTrigger(gameType, 50) to
                Animation("wolf", "/theme/halloween/horrific/wolf.png"),
            DartScoreTrigger(gameType, 25) to
                Animation("wolf", "/theme/halloween/horrific/wolf.png"),
        )
    }

private val lightOrange = Color.decode("#ff8200")
private val orange = Color.decode("#CF5704")
private val bloodRed = Color.decode("#880808")

private val halloweenDartboardColours =
    ColourWrapper(
        lightOrange,
        orange,
        orange,
        Color.decode("#009900"),
        Color.GREEN,
        Color.GREEN,
        orange,
        Color.GREEN,
        fontColor = bloodRed,
    )

private val bannerTextRenderer = simpleBannerRenderer(ThemeId.Halloween)

val Themes.HALLOWEEN: Theme
    get() =
        Theme(
            ThemeId.Halloween,
            "Zombies, eyeballs and witches have come to terrorise the app!",
            lightOrange,
            orange,
            Color.decode("#32172a"),
            lightBackground = Color.decode("#DAB1DA"),
            linkColour = Color.decode("#009900"),
            fontColor = bloodRed,
            dartboardColours = halloweenDartboardColours,
            bannerTextRenderer = bannerTextRenderer,
            animations = halloweenAnimations.toMap(),
            festivalInfo = FestivalInfo(::findHalloween, "Next haunting will start"),
            unlockDate = LocalDate.of(2026, Month.OCTOBER, 24),
        )

private fun findHalloween(year: Int): Pair<LocalDate, LocalDate> =
    LocalDate.of(year, Month.OCTOBER, 24) to LocalDate.of(year, Month.OCTOBER, 31)
