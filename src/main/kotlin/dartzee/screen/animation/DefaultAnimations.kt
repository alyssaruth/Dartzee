package dartzee.screen.animation

import dartzee.game.GameType
import kotlin.collections.map

val CHUCKLEVISION =
    CompositeAnimation((1..3).map { Animation("chucklevision$it", "/horrific/chuckle.png") })

val BRUCEY_BAD_LUCK =
    CompositeAnimation((1..2).map { Animation("badLuck$it", "/horrific/forsyth.png") })

val BRUCEY_BONUS =
    CompositeAnimation((1..4).map { Animation("forsyth$it", "/horrific/forsyth.png") })

val MISS_DEV = Animation("fourTrimmed", "/horrific/dev.png")

val BULLSEYE_DEV = Animation("bull", "/horrific/dev.png")

val BAD_MISS =
    CompositeAnimation(
        (1..4).map { Animation("badmiss$it", "/horrific/mitchell.png") } +
            Animation("damage", "/horrific/spencer.png")
    )

val BASIL_FAWLTY = CompositeAnimation((1..4).map { Animation("basil$it", "/horrific/basil.png") })

val DEFAULT_ANIMATIONS: Map<IAnimationTrigger, IAnimation> =
    mapOf(
        TotalScoreTrigger(GameType.X01, 60) to Animation("60", null),
        TotalScoreTrigger(GameType.X01, 100) to Animation("100", null),
        TotalScoreTrigger(GameType.X01, 140) to Animation("140", null),
        TotalScoreTrigger(GameType.X01, 180) to Animation("180", null),
        TotalScoreTrigger(GameType.X01, 69) to CHUCKLEVISION,
        TotalScoreTrigger(GameType.X01, 26) to BASIL_FAWLTY,
        BadLuckTrigger to BRUCEY_BAD_LUCK,
        BruceyBonusTrigger to BRUCEY_BONUS,
        DartScoreTrigger(GameType.GOLF, 0) to MISS_DEV,
        DartScoreTrigger(GameType.X01, 0) to BAD_MISS,
        DartScoreTrigger(GameType.ROUND_THE_CLOCK, 0) to BAD_MISS,
        DartScoreTrigger(GameType.DARTZEE, 0) to BAD_MISS,
    ) + GameType.values().map { DartScoreTrigger(it, 50) to BULLSEYE_DEV }
