package dartzee.theme

import dartzee.bean.DartLabel
import dartzee.core.util.toLocalDate
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.screen.animation.Animation
import dartzee.screen.animation.CompositeAnimation
import dartzee.screen.animation.DartScoreTrigger
import dartzee.screen.animation.IAnimation
import dartzee.screen.animation.IAnimationTrigger
import dartzee.screen.animation.TotalScoreTrigger
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.now
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.time.LocalDate
import javax.swing.ImageIcon

private val lightBrown = Color.decode("#542e15")
private val darkBrown = Color.decode("#4b270f")
private val lightPink = Color.decode("#b25f67")
private val darkPink = Color.decode("#813c3f")

private val candleColours = listOf("red", "yellow", "green", "blue", "pink", "purple")

private val dartboardColours =
    ColourWrapper(
        lightBrown,
        lightPink,
        darkPink,
        darkBrown,
        darkPink,
        lightPink,
        darkPink,
        lightPink,
        fontColor = Color.decode("#fff68f"),
    )

private fun dartFactory(pt: Point): DartLabel {
    val colour = candleColours[pt.x % candleColours.size]
    val icon = ImageIcon(Theme::class.java.getResource("/theme/birthday/darts/candle-$colour.png"))
    val label = DartLabel(icon).also { it.location = Point(pt.x, pt.y - icon.iconHeight) }
    return label
}

private fun partyPopperAnimation(age: Int) =
    CompositeAnimation(
        (1..3).map {
            Animation("party-popper", "/theme/birthday/horrific/party-popper-$it.png", "$age")
        }
    )

val Themes.BIRTHDAY: Theme
    get() =
        Theme(
            ThemeId.Birthday,
            "Celebrate with cake, balloons and confetti",
            Color.decode("#057684"),
            Color.decode("#6bbabe"),
            Color.decode("#a1cc9e"),
            menuFontSize = 26f,
            lightBackground = Color.decode("#FDFBD4"),
            dartboardColours = dartboardColours,
            bannerTextRenderer = ::getBannerDetails,
            dartFactory = ::dartFactory,
        )

private val birthdayAnimations: List<Pair<IAnimationTrigger, IAnimation>> =
    GameType.values().flatMap { gameType ->
        listOf(
            DartScoreTrigger(gameType, 0) to
                CompositeAnimation(
                    (1..2).map {
                        Animation("splat-$it", "/theme/birthday/horrific/dropped-cake.png")
                    }
                )
        )
    }

fun makeBirthdayTheme(): Theme {
    val ages = InjectedThings.birthdayInfo?.ages?.distinct().orEmpty()

    val animations = birthdayAnimations + ages.flatMap(::animationsForAge)
    return Themes.BIRTHDAY.copy(animations = animations.toMap())
}

private fun animationsForAge(age: Int) =
    GameType.values().flatMap { gameType ->
        listOf(
            DartScoreTrigger(gameType, age) to partyPopperAnimation(age),
            TotalScoreTrigger(gameType, age) to partyPopperAnimation(age),
        )
    }

private fun getBannerDetails(
    svgBounds: Rectangle,
    dartboardCenter: Point,
): List<BannerRenderDetails> {
    val center = Point(dartboardCenter.x, dartboardCenter.y - (0.3 * svgBounds.height).toInt())
    val topBanner = BannerRenderDetails("Happy Birthday", (svgBounds.height * 0.1).toInt(), center)
    val names = InjectedThings.birthdayInfo?.namesString()

    val bottomBanner =
        names?.let {
            BannerRenderDetails(
                "$names!",
                (svgBounds.height * 0.1).toInt(),
                Point(dartboardCenter.x, dartboardCenter.y - (0.15 * svgBounds.height).toInt()),
                maxWidth = (svgBounds.width * 0.85).toInt(),
            )
        }

    return listOfNotNull(topBanner, bottomBanner)
}

fun computeBirthdayInfo(): BirthdayInfo? {
    val players = PlayerEntity.retrievePlayers("").filter { it.birthdayIsToday() }
    if (players.isEmpty()) {
        return null
    }

    val names = players.map { it.name }
    val ages = players.map { now.year - it.dateOfBirth.toLocalDateTime().year }

    return BirthdayInfo(names, ages)
}

fun getExtraBirthdayDescription(): String {
    val birthdayInfo = InjectedThings.birthdayInfo
    if (birthdayInfo != null) {
        return "Active today for ${InjectedThings.birthdayInfo?.namesString()}!"
    } else {
        val birthDateToPlayer =
            PlayerEntity.retrievePlayers("").mapNotNull { player ->
                val birthDate = player.dateOfBirth.toLocalDate()
                birthDate?.let {
                    var nextBirthday = LocalDate.of(now.year, birthDate.month, birthDate.dayOfMonth)
                    if (nextBirthday.isBefore(now)) {
                        nextBirthday =
                            LocalDate.of(now.year + 1, birthDate.month, birthDate.dayOfMonth)
                    }

                    nextBirthday to player.name
                }
            }

        if (birthDateToPlayer.isEmpty()) {
            return ""
        } else {
            val nextBirthDate = birthDateToPlayer.minBy { it.first }.first
            val players = birthDateToPlayer.filter { it.first == nextBirthDate }.map { it.second }
            return "Next celebrated on ${nextBirthDate.fmt()} for ${players.joinToString(" & ")}!"
        }
    }
}
