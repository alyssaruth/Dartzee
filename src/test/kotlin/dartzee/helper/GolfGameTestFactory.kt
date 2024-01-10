package dartzee.helper

import dartzee.drtDoubleEight
import dartzee.drtDoubleEighteen
import dartzee.drtDoubleEleven
import dartzee.drtDoubleFour
import dartzee.drtDoubleOne
import dartzee.drtDoubleSeven
import dartzee.drtDoubleSeventeen
import dartzee.drtDoubleSix
import dartzee.drtDoubleSixteen
import dartzee.drtDoubleTen
import dartzee.drtDoubleThree
import dartzee.drtDoubleTwo
import dartzee.drtInnerFive
import dartzee.drtInnerFourteen
import dartzee.drtInnerOne
import dartzee.drtInnerSixteen
import dartzee.drtInnerThree
import dartzee.drtInnerTwelve
import dartzee.drtMissEight
import dartzee.drtMissFive
import dartzee.drtMissFour
import dartzee.drtMissFourteen
import dartzee.drtMissNine
import dartzee.drtMissOne
import dartzee.drtMissSeven
import dartzee.drtMissSeventeen
import dartzee.drtMissSix
import dartzee.drtMissSixteen
import dartzee.drtMissTen
import dartzee.drtMissThirteen
import dartzee.drtMissTwenty
import dartzee.drtMissTwo
import dartzee.drtOuterEight
import dartzee.drtOuterEighteen
import dartzee.drtOuterEleven
import dartzee.drtOuterFifteen
import dartzee.drtOuterFive
import dartzee.drtOuterFour
import dartzee.drtOuterNine
import dartzee.drtOuterNineteen
import dartzee.drtOuterOne
import dartzee.drtOuterSeven
import dartzee.drtOuterSeventeen
import dartzee.drtOuterSix
import dartzee.drtOuterSixteen
import dartzee.drtOuterTen
import dartzee.drtOuterThirteen
import dartzee.drtOuterThree
import dartzee.drtOuterTwelve
import dartzee.drtOuterTwo
import dartzee.drtTrebleFive
import dartzee.drtTrebleFourteen
import dartzee.drtTrebleSeven
import dartzee.drtTrebleTwo
import dartzee.`object`.Dart
import dartzee.stats.GameWrapper
import java.sql.Timestamp

private fun golfFrontNine22Rounds(): List<List<Dart>> =
    listOf(
            listOf(drtDoubleOne()), // 1
            listOf(drtOuterFifteen(), drtOuterTwo()), // 4
            listOf(drtDoubleThree()), // 1
            listOf(drtOuterEighteen(), drtMissFour(), drtOuterFour()), // 4
            listOf(drtMissFive(), drtOuterFive()), // 4
            listOf(drtDoubleSix()), // 1
            listOf(drtMissSeven(), drtDoubleSeven()), // 1
            listOf(drtInnerSixteen(), drtDoubleEight()), // 1
            listOf(drtOuterTwelve(), drtTrebleFourteen(), drtMissNine()), // 5
        )
        .also(::setRoundNumbers)

fun golfFrontNine22(localId: Long = 1L, dtStart: Timestamp = Timestamp(1000)): GameWrapper {
    val rounds = golfFrontNine22Rounds()

    return makeGolfGameWrapper(
        localId,
        gameParams = "9",
        expectedScore = 22,
        dartRounds = rounds,
        dtStart = dtStart
    )
}

fun golfFrontNine22EvenRounds(): GameWrapper {
    val rounds = golfFrontNine22Rounds().filterIndexed { ix, _ -> ix % 2 != 0 }
    return makeGameWrapper(gameParams = "9", teamGame = true).also {
        rounds.forEach { round -> round.forEach(it::addDart) }
    }
}

fun golfFrontNine29(localId: Long = 1L, dtStart: Timestamp = Timestamp(1000)): GameWrapper {
    val rounds =
        listOf(
            listOf(drtMissOne(), drtMissTwenty(), drtOuterOne()), // 4
            listOf(drtOuterTwo()), // 4
            listOf(drtOuterThree(), drtOuterThree()), // 4
            listOf(drtOuterEighteen(), drtMissFour(), drtOuterFour()), // 4
            listOf(drtMissFive(), drtTrebleFive()), // 2
            listOf(drtDoubleTen(), drtDoubleSix()), // 1
            listOf(drtMissSeven(), drtOuterNineteen(), drtTrebleSeven()), // 2
            listOf(drtInnerSixteen(), drtOuterSixteen(), drtOuterEight()), // 4
            listOf(drtOuterNine()), // 4
        )

    return makeGolfGameWrapper(
        localId,
        gameParams = "9",
        expectedScore = 29,
        dartRounds = rounds,
        dtStart = dtStart
    )
}

fun golfFull31_22(localId: Long = 1L, dtStart: Timestamp = Timestamp(1000)): GameWrapper {
    val rounds =
        listOf(
            listOf(drtMissOne(), drtMissTwenty(), drtInnerOne()), // 3
            listOf(drtMissTwo(), drtDoubleTwo()), // 1
            listOf(drtInnerThree()), // 3
            listOf(drtOuterFour(), drtOuterFour()), // 4
            listOf(drtOuterFive(), drtMissFive(), drtMissFive()), // 5
            listOf(drtOuterSix(), drtMissSix(), drtOuterSix()), // 4
            listOf(drtOuterSeven(), drtTrebleSeven()), // 2
            listOf(drtMissEight(), drtOuterEight(), drtOuterSixteen()), // 5
            listOf(drtMissNine(), drtOuterNine()), // 4
            // Halfway - 31
            listOf(drtMissTen(), drtDoubleTen()), // 1
            listOf(drtDoubleEleven()), // 1
            listOf(drtInnerTwelve()), // 3
            listOf(drtMissThirteen(), drtOuterFour(), drtOuterSix()), // 5
            listOf(drtOuterEleven(), drtMissFourteen(), drtMissFourteen()), // 5
            listOf(drtOuterFifteen(), drtOuterTen(), drtOuterFifteen()), // 4
            listOf(drtMissSixteen(), drtDoubleSixteen()), // 1
            listOf(drtMissSeventeen(), drtOuterThree(), drtDoubleSeventeen()), // 1
            listOf(drtDoubleEighteen()), // 1
        )

    return makeGolfGameWrapper(
        localId,
        gameParams = "18",
        expectedScore = 53,
        dartRounds = rounds,
        dtStart = dtStart
    )
}

fun golfFull28_29(localId: Long = 1L, dtStart: Timestamp = Timestamp(1000)): GameWrapper {
    val rounds =
        listOf(
            listOf(drtMissOne(), drtOuterOne()), // 4
            listOf(drtTrebleTwo()), // 2
            listOf(drtDoubleThree()), // 1
            listOf(drtDoubleFour()), // 1
            listOf(drtMissFive(), drtMissFive(), drtInnerFive()), // 3
            listOf(drtMissSix(), drtOuterSix()), // 4
            listOf(drtOuterSeven()), // 4
            listOf(drtMissEight(), drtOuterSeven(), drtOuterSixteen()), // 5
            listOf(drtMissNine(), drtOuterTwelve(), drtOuterNine()), // 4
            // Halfway - 28
            listOf(drtDoubleTen()), // 1
            listOf(drtOuterEleven()), // 4
            listOf(drtInnerTwelve()), // 3
            listOf(drtOuterThirteen()), // 4
            listOf(drtInnerFourteen()), // 3
            listOf(drtOuterFifteen()), // 4
            listOf(drtMissSixteen(), drtMissSixteen(), drtMissSixteen()), // 5
            listOf(drtMissSeventeen(), drtOuterSeventeen()), // 4
            listOf(drtDoubleEighteen()), // 1
        )

    return makeGolfGameWrapper(
        localId,
        gameParams = "18",
        expectedScore = 57,
        dartRounds = rounds,
        dtStart = dtStart
    )
}

fun golfFullOptimal(): Pair<GameWrapper, List<Long>> {
    val rounds =
        listOf(
            listOf(drtDoubleOne()), // 1, from G1
            listOf(drtMissTwo(), drtDoubleTwo()), // 1, from G3
            listOf(drtDoubleThree()), // 1, from G1
            listOf(drtDoubleFour()), // 1, from G4
            listOf(drtMissFive(), drtTrebleFive()), // 2, from G2
            listOf(drtDoubleSix()), // 1, from G1
            listOf(drtMissSeven(), drtDoubleSeven()), // 1, from G1
            listOf(drtInnerSixteen(), drtDoubleEight()), // 1, from G1
            listOf(drtOuterNine()), // 4, from G2
            // Halfway - 13
            listOf(drtDoubleTen()), // 1, from G4 (fewer darts)
            listOf(drtDoubleEleven()), // 1, from G3
            listOf(drtInnerTwelve()), // 3, from G3 (equivalent, but G3 happened first)
            listOf(drtOuterThirteen()), // 4, from G4
            listOf(drtInnerFourteen()), // 3, from G4
            listOf(drtOuterFifteen()), // 4, from G4 (fewer darts)
            listOf(drtMissSixteen(), drtDoubleSixteen()), // 1, from G3
            listOf(drtMissSeventeen(), drtOuterThree(), drtDoubleSeventeen()), // 1, from G3
            listOf(drtDoubleEighteen()), // 1, from G3
        )

    return makeGolfGameWrapper(1L, gameParams = "18", expectedScore = 32, dartRounds = rounds) to
        listOf(1L, 3L, 1L, 4L, 2L, 1L, 1L, 1L, 2L, 4L, 3L, 3L, 4L, 4L, 4L, 3L, 3L, 3L)
}

fun golfAllMisses(): GameWrapper {
    val rounds = (1..18).map { listOf(Dart(20, 0), Dart(20, 0), Dart(20, 0)) }
    return makeGolfGameWrapper(-1, gameParams = "18", expectedScore = 90, dartRounds = rounds)
}
