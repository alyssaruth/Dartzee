package dartzee.screen.animation

import dartzee.game.GameType

val DEFAULT_ANIMATIONS: Map<IAnimationTrigger, Animation> =
    mapOf(
        TotalScoreTrigger(GameType.X01, 60) to Animation("60", null),
        TotalScoreTrigger(GameType.X01, 100) to Animation("100", null),
        TotalScoreTrigger(GameType.X01, 140) to Animation("140", null),
        TotalScoreTrigger(GameType.X01, 180) to Animation("180", null),
        TotalScoreTrigger(GameType.X01, 69) to
            Animation((1..3).map { "chucklevision$it" }, "/horrific/chuckle.png"),
    )
