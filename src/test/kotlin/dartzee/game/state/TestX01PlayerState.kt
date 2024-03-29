package dartzee.game.state

import dartzee.drtDoubleFifteen
import dartzee.drtDoubleFive
import dartzee.drtDoubleSeventeen
import dartzee.drtMissFifteen
import dartzee.drtMissTwenty
import dartzee.drtOuterOne
import dartzee.drtOuterTen
import dartzee.drtTrebleTwenty
import dartzee.game.FinishType
import dartzee.game.X01Config
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.insertTeam
import dartzee.helper.makeDart
import dartzee.helper.makeX01PlayerStateWithRounds
import dartzee.helper.makeX01Rounds
import dartzee.`object`.Dart
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestX01PlayerState : AbstractTest() {
    @Test
    fun `should report correct score if no darts thrown`() {
        val state = makeX01PlayerStateWithRounds(completedRounds = listOf())
        state.getScoreSoFar() shouldBe 0
    }

    @Test
    fun `should count completed rounds as 3 darts, regardless of how many were actually thrown`() {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        val roundTwo = listOf(Dart(20, 3))
        val roundThree = listOf(Dart(20, 1), Dart(20, 2))

        val state =
            makeX01PlayerStateWithRounds(completedRounds = listOf(roundOne, roundTwo, roundThree))
        state.getScoreSoFar() shouldBe 9
    }

    @Test
    fun `Should not count the finishing round as 3 darts if it contains less`() {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        val roundTwo = listOf(Dart(20, 3))
        val roundThree = listOf(Dart(20, 1), makeDart(20, 2, startingScore = 40))

        val state =
            makeX01PlayerStateWithRounds(completedRounds = listOf(roundOne, roundTwo, roundThree))
        state.getScoreSoFar() shouldBe 8
    }

    @Test
    fun `Should not count the finishing round as 3 darts in relaxed mode`() {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        val roundTwo = listOf(Dart(20, 3))
        val roundThree = listOf(Dart(20, 2), makeDart(20, 1, startingScore = 20))

        val state =
            makeX01PlayerStateWithRounds(
                completedRounds = listOf(roundOne, roundTwo, roundThree),
                finishType = FinishType.Any
            )
        state.getScoreSoFar() shouldBe 8
    }

    @Test
    fun `should add on darts from the in progress round`() {
        val roundOne = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        val roundTwo = listOf(Dart(20, 3))

        val state = makeX01PlayerStateWithRounds(completedRounds = listOf(roundOne, roundTwo))
        state.getScoreSoFar() shouldBe 6

        state.dartThrown(Dart(20, 1))
        state.getScoreSoFar() shouldBe 7

        state.dartThrown(Dart(5, 1))
        state.getScoreSoFar() shouldBe 8

        state.resetRound()
        state.getScoreSoFar() shouldBe 6
    }

    @Test
    fun `The remaining score should be the starting score if no darts have been thrown`() {
        val state = makeX01PlayerStateWithRounds(501, completedRounds = listOf())
        state.getRemainingScore() shouldBe 501

        val state301 = makeX01PlayerStateWithRounds(301, completedRounds = listOf())
        state301.getRemainingScore() shouldBe 301
    }

    @Test
    fun `Should correctly compute the current remaining score, taking into account busts`() {
        val roundOne = listOf(Dart(20, 3), Dart(20, 3), Dart(20, 3)) // 121
        val roundTwo = listOf(Dart(20, 3), Dart(20, 3)) // bust, 121
        val roundThree = listOf(Dart(20, 3), Dart(20, 1), Dart(1, 1)) // 40
        val roundFour = listOf(Dart(20, 1), Dart(10, 1), Dart(5, 1)) // 5
        val roundFive = listOf(Dart(20, 1)) // bust, 5
        val roundSix = listOf(Dart(1, 1)) // 4, mercied

        val rounds =
            makeX01Rounds(301, roundOne, roundTwo, roundThree, roundFour, roundFive, roundSix)

        val state = makeX01PlayerStateWithRounds(301, completedRounds = rounds)
        state.getRemainingScoreForRound(1) shouldBe 121
        state.getRemainingScoreForRound(2) shouldBe 121
        state.getRemainingScoreForRound(3) shouldBe 40
        state.getRemainingScoreForRound(4) shouldBe 5
        state.getRemainingScoreForRound(5) shouldBe 5
        state.getRemainingScoreForRound(6) shouldBe 4

        state.getRemainingScore() shouldBe 4
    }

    @Test
    fun `Should take into account current round when computing the remaining score`() {
        val state =
            X01PlayerState(
                X01Config(301, FinishType.Doubles),
                SingleParticipant(insertParticipant())
            )
        state.dartThrown(makeDart(20, 3))
        state.dartThrown(makeDart(20, 3))
        state.dartThrown(makeDart(20, 3))

        state.getRemainingScore() shouldBe 121
        state.commitRound()
        state.getRemainingScore() shouldBe 121

        state.dartThrown(makeDart(20, 3))
        state.dartThrown(makeDart(20, 3))
        state.getRemainingScore() shouldBe 1
        state.isCurrentRoundComplete() shouldBe true

        state.commitRound()
        state.getRemainingScore() shouldBe 121
    }

    @Test
    fun `should compute the remaining score correctly when finishType is Any`() {
        val state =
            X01PlayerState(X01Config(101, FinishType.Any), SingleParticipant(insertParticipant()))

        state.dartThrown(makeDart(20, 3))
        state.dartThrown(makeDart(20, 1))
        state.dartThrown(makeDart(20, 1))
        state.getRemainingScore() shouldBe 1

        state.commitRound()
        state.getRemainingScore() shouldBe 1
    }

    @Test
    fun `Should return a bad luck count of 0 if no darts thrown`() {
        val state = makeX01PlayerStateWithRounds(completedRounds = emptyList())
        state.getBadLuckCount() shouldBe 0
    }

    @Test
    fun `Should compute bad luck count correctly based on all thrown darts`() {
        val roundOne =
            listOf(
                makeDart(startingScore = 40, score = 5, multiplier = 2), // bad luck
                makeDart(startingScore = 30, score = 10, multiplier = 1),
                makeDart(startingScore = 20, score = 15, multiplier = 0)
            )

        val roundTwo = listOf(makeDart(startingScore = 20, score = 17, multiplier = 2))

        val roundThree =
            listOf(
                makeDart(startingScore = 20, score = 15, multiplier = 2) // bad luck
            )

        val state =
            makeX01PlayerStateWithRounds(completedRounds = listOf(roundOne, roundTwo, roundThree))
        state.getBadLuckCount() shouldBe 2

        val dart = makeDart(score = 6, multiplier = 2)
        state.dartThrown(dart)
        dart.startingScore = 20
        state.getBadLuckCount() shouldBe 3

        state.resetRound()
        state.getBadLuckCount() shouldBe 2
    }

    @Test
    fun `Should compute bad luck count for the current player`() {
        val team = insertTeam()
        val pt1 = insertParticipant(teamId = team.rowId)
        val pt2 = insertParticipant(teamId = team.rowId)
        val state =
            X01PlayerState(
                X01Config(101, FinishType.Doubles),
                TeamParticipant(team, listOf(pt1, pt2))
            )

        val roundOne = listOf(drtTrebleTwenty(), drtOuterOne(), drtMissTwenty()) // Get down to 40

        val roundTwo =
            listOf(
                drtDoubleFive(), // bad luck
                drtOuterTen(),
                drtMissFifteen()
            )

        val roundThree = listOf(drtDoubleSeventeen())

        val roundFour = listOf(drtDoubleFifteen())

        roundOne.forEach(state::dartThrown)
        state.commitRound()

        roundTwo.forEach(state::dartThrown)
        state.getBadLuckCount() shouldBe 1
        state.commitRound()

        state.getBadLuckCount() shouldBe 0
        roundThree.forEach(state::dartThrown)
        state.getBadLuckCount() shouldBe 0
        state.commitRound()

        state.getBadLuckCount() shouldBe 1
        roundFour.forEach(state::dartThrown)
        state.getBadLuckCount() shouldBe 2
    }

    @Test
    fun `Should return the last committed round`() {
        val roundOne = listOf(Dart(20, 1))
        val roundTwo = listOf(Dart(5, 1), Dart(1, 1))
        val roundThree = listOf(Dart(20, 1), Dart(20, 1), Dart(5, 1))

        val state =
            makeX01PlayerStateWithRounds(completedRounds = listOf(roundOne, roundTwo, roundThree))
        state.getLastRound() shouldBe roundThree
    }

    @Test
    fun `Should set startingScore on darts as they are added`() {
        val state =
            X01PlayerState(
                X01Config(301, FinishType.Doubles),
                SingleParticipant(insertParticipant())
            )

        val dartOne = makeDart(20, 1)
        val dartTwo = makeDart(25, 2)
        val dartThree = makeDart(1, 1)

        state.dartThrown(dartOne)
        state.dartThrown(dartTwo)
        state.dartThrown(dartThree)

        dartOne.startingScore shouldBe 301
        dartTwo.startingScore shouldBe 281
        dartThree.startingScore shouldBe 231
    }

    @Test
    fun `Should correctly determine whether the current round is completed`() {
        val threeDartRound = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        val bustRound = listOf(Dart(20, 3), Dart(20, 3))
        val finishedRound = listOf(Dart(17, 3), Dart(25, 2))
        val incompleteRound = listOf(Dart(20, 1), Dart(20, 1))
        val leftOneRemaining = listOf(Dart(20, 3), Dart(20, 2))

        stateWithCurrentRound(threeDartRound).isCurrentRoundComplete() shouldBe true
        stateWithCurrentRound(bustRound).isCurrentRoundComplete() shouldBe true
        stateWithCurrentRound(finishedRound).isCurrentRoundComplete() shouldBe true
        stateWithCurrentRound(leftOneRemaining).isCurrentRoundComplete() shouldBe true
        stateWithCurrentRound(incompleteRound).isCurrentRoundComplete() shouldBe false
        stateWithCurrentRound(emptyList()).isCurrentRoundComplete() shouldBe false

        val finishedOnTreble = listOf(Dart(20, 3), Dart(20, 1), Dart(7, 3))
        stateWithCurrentRound(bustRound, FinishType.Any).isCurrentRoundComplete() shouldBe true
        stateWithCurrentRound(finishedRound, FinishType.Any).isCurrentRoundComplete() shouldBe true
        stateWithCurrentRound(finishedOnTreble, FinishType.Any).isCurrentRoundComplete() shouldBe
            true

        stateWithCurrentRound(leftOneRemaining, FinishType.Any).isCurrentRoundComplete() shouldBe
            false
        stateWithCurrentRound(incompleteRound, FinishType.Any).isCurrentRoundComplete() shouldBe
            false
        stateWithCurrentRound(emptyList(), FinishType.Any).isCurrentRoundComplete() shouldBe false
    }

    private fun stateWithCurrentRound(
        darts: List<Dart>,
        finishType: FinishType = FinishType.Doubles
    ): X01PlayerState {
        val state =
            X01PlayerState(X01Config(101, finishType), SingleParticipant(insertParticipant()))
        darts.forEach { state.dartThrown(it) }
        return state
    }
}
