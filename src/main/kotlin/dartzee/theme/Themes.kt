package dartzee.theme

import dartzee.game.GameType
import dartzee.`object`.ColourWrapper
import dartzee.screen.animation.Animation
import dartzee.screen.animation.CompositeAnimation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.screen.animation.PlayerVictory
import java.awt.Color

private val lightOrange = Color.decode("#ff8200")
private val orange = Color.decode("#CF5704")
private val pastelGreen = Color.decode("#dcf9a8")
private val bloodRed = Color.decode("#880808")
private val easterYellow = Color.decode("#ffebaf")

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

private val easterDartboardColours =
    ColourWrapper(
        easterYellow.darker(),
        easterYellow,
        easterYellow,
        pastelGreen,
        pastelGreen.darker(),
        pastelGreen.darker(),
        easterYellow,
        pastelGreen.darker(),
    )

private val oktoberfestDartboardColours =
    ColourWrapper(
        Color.YELLOW,
        Color.RED,
        Color.RED,
        Color.RED,
        Color.YELLOW,
        Color.YELLOW,
        Color.RED,
        Color.YELLOW,
        fontColor = Color.decode("#e3570a"),
    )

object Themes {
    private val witchAnimation =
        CompositeAnimation(
            (1..3).map { Animation("witch$it", "/theme/halloween/horrific/witch.png") }
        )

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

    val HALLOWEEN =
        Theme(
            ThemeId.halloween,
            lightOrange,
            orange,
            Color.decode("#32172a"),
            lightBackground = Color.decode("#DAB1DA"),
            linkColour = Color.decode("#009900"),
            fontColor = bloodRed,
            dartboardColours = halloweenDartboardColours,
            animations = halloweenAnimations.toMap(),
        )

    private val easterAnimations: List<Pair<IAnimationTrigger, IAnimation>> =
        listOf(
            PlayerVictory to Animation("thats-all-folks", "/theme/easter/horrific/bugs-bunny.png")
        ) +
            GameType.values().flatMap { gameType ->
                listOf(
                    DartScoreTrigger(gameType, 0) to
                        Animation("egg-crack", "/theme/easter/horrific/egg-crack.png")
                )
            }

    val EASTER =
        Theme(
            ThemeId.easter,
            Color.decode("#c1f0fb"),
            Color.decode("#8aadd3"),
            Color.decode("#e0cdff"),
            Color.decode("#f9ceee"),
            linkColour = Color.BLUE,
            fontColor = Color.DARK_GRAY,
            dartboardColours = easterDartboardColours,
            menuFontSize = 20f,
            animations = easterAnimations.toMap(),
            bannerScaleFactor = 0.5,
            bannerOffset = 0.3,
        )

    private val beerSmashAnimation =
        CompositeAnimation(
            (1..3).map { Animation("smash$it", "/theme/oktoberfest/horrific/dropped-beer.png") } +
                (1..3).map {
                    Animation("smash$it", "/theme/oktoberfest/horrific/dropped-beer-2.png")
                }
        )

    private val oktoberfestAnimations: List<Pair<IAnimationTrigger, IAnimation>> =
        GameType.values().flatMap { gameType ->
            listOf(
                DartScoreTrigger(gameType, 9) to
                    Animation("nine-pints", "/theme/oktoberfest/horrific/pints-of-lager.png"),
                DartScoreTrigger(gameType, 10) to
                    Animation("blimey", "/theme/oktoberfest/horrific/blimey.png"),
                DartScoreTrigger(gameType, 18) to
                    Animation("eighteen-pints", "/theme/oktoberfest/horrific/pints-of-lager.png"),
                DartScoreTrigger(gameType, 0) to beerSmashAnimation,
            )
        }

    val OKTOBERFEST =
        Theme(
            ThemeId.oktoberfest,
            Color.decode("#e3570a"),
            Color.decode("#764d2a"),
            Color.decode("#fbb40e"),
            Color.decode("#ffcf60"),
            linkColour = bloodRed,
            dartboardColours = oktoberfestDartboardColours,
            menuFontSize = 24f,
            animations = oktoberfestAnimations.toMap(),
        )
}
