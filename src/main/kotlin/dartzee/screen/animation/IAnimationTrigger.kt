package dartzee.screen.animation

import dartzee.game.GameType

interface IAnimationTrigger {}

data class TotalScoreTrigger(val gameType: GameType, val total: Int) : IAnimationTrigger

data class DartScoreTrigger(val gameType: GameType, val total: Int) : IAnimationTrigger

sealed class BadLuckTrigger : IAnimationTrigger
