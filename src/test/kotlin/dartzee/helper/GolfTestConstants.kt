package dartzee.helper

import dartzee.drtDoubleFive
import dartzee.drtDoubleNine
import dartzee.drtDoubleSeven
import dartzee.drtDoubleThree
import dartzee.drtInnerEight
import dartzee.drtInnerEleven
import dartzee.drtInnerOne
import dartzee.drtInnerSix
import dartzee.drtInnerSixteen
import dartzee.drtInnerThree
import dartzee.drtMissEight
import dartzee.drtMissEighteen
import dartzee.drtMissFour
import dartzee.drtMissNine
import dartzee.drtMissOne
import dartzee.drtMissSeven
import dartzee.drtMissTwo
import dartzee.drtOuterEighteen
import dartzee.drtOuterFifteen
import dartzee.drtOuterFive
import dartzee.drtOuterOne
import dartzee.drtOuterSeven
import dartzee.drtOuterSeventeen
import dartzee.drtOuterSix
import dartzee.drtOuterSixteen
import dartzee.drtOuterThree
import dartzee.drtOuterTwenty
import dartzee.drtOuterTwo
import dartzee.drtTrebleFour
import dartzee.drtTrebleNine
import dartzee.drtTrebleSeventeen

private val gameOneRounds = listOf(
    listOf(drtOuterOne(), drtInnerOne()), // 3, 1 gambled
    listOf(drtOuterFifteen(), drtTrebleSeventeen(), drtOuterSeventeen()), // 8, 1 gambled
    listOf(drtInnerThree(), drtOuterThree(), drtDoubleThree()), // 9, 4 gambled
    listOf(drtTrebleFour()), // 11, 4 gambled
    listOf(drtDoubleFive()), // 12, 4 gambled
    listOf(drtOuterSix(), drtOuterSix(), drtOuterSix()), // 16, 6 gambled
    listOf(drtOuterSeven(), drtOuterSixteen(), drtInnerSixteen()), // 21, 7 gambled
    listOf(drtMissEight(), drtInnerEight()), // 24, 7 gambled
    listOf(drtMissNine(), drtDoubleNine()), // 25, 7 gambled
).also { setRoundNumbers(it) }

val GAME_WRAPPER_GOLF_9 = makeGolfGameWrapper(localId = 1L, gameParams = "9", dartRounds = gameOneRounds, expectedScore = 25)

val GAME_WRAPPER_GOLF_9_EVEN_ROUNDS = makeGameWrapper(localId = 1L, gameParams = "9", teamGame = true).also {
    gameOneRounds.forEachIndexed { ix, round ->
        if (ix % 2 != 0)
        {
            round.forEach(it::addDart)
        }
    }
}

private val gameTwoRounds = listOf(
    listOf(drtOuterOne(), drtMissOne(), drtMissOne()), // 5, 1 gambled
    listOf(drtOuterTwo(), drtMissTwo(), drtOuterTwo()), // 9, 2 gambled
    listOf(drtInnerThree()),  // 12, 2 gambled
    listOf(drtMissFour(), drtOuterEighteen(), drtMissEighteen()), // 17, 2 gambled
    listOf(drtOuterTwenty(), drtOuterFive()), // 21, 2 gambled
    listOf(drtOuterSix(), drtInnerSix()), // 24, 3 gambled
    listOf(drtMissSeven(), drtDoubleSeven()), // 25, 3 gambled
    listOf(drtInnerEight()), // 28, 3 gambled
    listOf(drtMissNine(), drtInnerEleven(), drtTrebleNine()), // 30, 3 gambled
).also { setRoundNumbers(it) }

val GAME_WRAPPER_GOLF_9_2 = makeGolfGameWrapper(localId = 2L, gameParams = "9", dartRounds = gameTwoRounds, expectedScore = 30)