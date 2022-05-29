package dartzee.screen.stats.player.golf

import dartzee.*
import dartzee.helper.makeGolfGameWrapper
import dartzee.stats.GameWrapper

fun golfFrontNine22(localId: Long = 1L): GameWrapper
{
    val rounds = listOf(
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

    return makeGolfGameWrapper(localId, gameParams = "9", expectedScore = 22, dartRounds = rounds)
}

fun golfFrontNine29(localId: Long = 1L): GameWrapper
{
    val rounds = listOf(
        listOf(drtMissOne(), drtMissTwenty(), drtOuterOne()), // 4
        listOf(drtOuterTwo(), drtOuterTwo()), // 4
        listOf(drtOuterThree(), drtOuterThree()), // 4
        listOf(drtOuterEighteen(), drtMissFour(), drtOuterFour()), // 4
        listOf(drtMissFive(), drtTrebleFive()), // 2
        listOf(drtDoubleTen(), drtDoubleSix()), // 1
        listOf(drtMissSeven(), drtOuterNineteen(), drtTrebleSeven()), // 2
        listOf(drtInnerSixteen(), drtOuterSixteen(), drtOuterEight()), // 4
        listOf(drtOuterNine()), // 4
    )

    return makeGolfGameWrapper(localId, gameParams = "9", expectedScore = 29, dartRounds = rounds)
}

fun golfFull31_22(localId: Long = 1L): GameWrapper
{
    val rounds = listOf(
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

    return makeGolfGameWrapper(localId, gameParams = "18", expectedScore = 53, dartRounds = rounds)
}

fun golfFull28_29(localId: Long = 1L): GameWrapper
{
    val rounds = listOf(
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

    return makeGolfGameWrapper(localId, gameParams = "18", expectedScore = 57, dartRounds = rounds)
}