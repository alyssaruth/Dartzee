package burlton.dartzee.code.dartzee

data class DartzeeRoundResult(val ruleNumber: Int,
                              val success: Boolean,
                              val userInputNeeded: Boolean = false,
                              val score: Int = -1)