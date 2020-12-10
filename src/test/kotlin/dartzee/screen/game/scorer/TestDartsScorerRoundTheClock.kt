package dartzee.screen.game.scorer

import com.github.alexburlton.swingtest.findChild
import dartzee.`object`.Dart
import dartzee.`object`.DartNotThrown
import dartzee.achievements.rtc.AchievementClockBestGame
import dartzee.firstRow
import dartzee.game.ClockType
import dartzee.game.RoundTheClockConfig
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.makeClockPlayerState
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TestDartsScorerRoundTheClock: AbstractTest()
{
    @Test
    fun `Should cope with empty state`()
    {
        val state = makeClockPlayerState()
        val scorer = factoryScorer()

        scorer.stateChanged(state)
        scorer.tableScores.rowCount shouldBe 0
    }

    @Test
    fun `Should render completed rounds correctly`()
    {
        val roundOne = listOf(Dart(1, 1), Dart(2, 1), Dart(3, 1), Dart(4, 0))
        val roundTwo = listOf(Dart(4, 1), Dart(5, 0), Dart(5, 3))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne, roundTwo))
        val scorer = factoryScorer()
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactly(
                roundOne,
                roundTwo + DartNotThrown()
        )

        scorer.lblResult.text shouldBe "7 Darts"
    }

    @Test
    fun `Should include the in progress round`()
    {
        val roundOne = listOf(Dart(1, 0), Dart(20, 1), Dart(5, 1))

        val state = makeClockPlayerState(completedRounds = listOf(roundOne))
        state.dartThrown(Dart(1, 1))
        val scorer = factoryScorer()
        scorer.stateChanged(state)

        val rows = scorer.tableScores.getRows()
        rows.shouldContainExactly(
                roundOne + DartNotThrown(),
                listOf(Dart(1, 1), null, null, null)
        )

        scorer.lblResult.text shouldBe "4 Darts"
    }

    @Test
    fun `Should signal when brucey bonus is no longer possible for the current round`()
    {
        val scorer = factoryScorer()
        val state = makeClockPlayerState()
        state.dartThrown(Dart(1, 1))

        scorer.stateChanged(state)
        scorer.tableScores.firstRow().shouldContainExactly(
                Dart(1, 1), null, null, null
        )

        state.dartThrown(Dart(17, 1))
        scorer.stateChanged(state)
        scorer.tableScores.firstRow().shouldContainExactly(
                Dart(1, 1), Dart(17, 1), null, DartNotThrown()
        )
    }

    @Test
    fun `Should not black out brucey bonus for current round if 4th dart was thrown`()
    {
        val scorer = factoryScorer()
        val state = makeClockPlayerState()

        val darts = listOf(Dart(1, 1), Dart(2, 1), Dart(3, 1), Dart(4, 0))
        darts.forEach { state.dartThrown(it) }

        scorer.stateChanged(state)
        scorer.tableScores.firstRow().shouldContainExactly(
                darts
        )
    }

    @Test
    fun `Should only add the scorecard if not in order`()
    {
        val scorerInOrder = factoryScorer(inOrder = true)
        scorerInOrder.findChild<RoundTheClockScorecard>().shouldBeNull()

        val scorerAnyOrder = factoryScorer(inOrder = false)
        scorerAnyOrder.findChild<RoundTheClockScorecard>().shouldNotBeNull()
    }

    @Test
    fun `Should add back the scorecard when achievement popup is dismissed`()
    {
        val scorer = factoryScorer(inOrder = false)
        scorer.achievementUnlocked(AchievementClockBestGame())

        scorer.findChild<RoundTheClockScorecard>().shouldBeNull()
        scorer.getAchievementOverlay()!!.close()
        scorer.findChild<RoundTheClockScorecard>().shouldNotBeNull()
    }

    private fun factoryScorer(clockType: ClockType = ClockType.Standard, inOrder: Boolean = true): DartsScorerRoundTheClock
    {
        val scorer = DartsScorerRoundTheClock(mockk(relaxed = true), RoundTheClockConfig(clockType, inOrder))
        scorer.init(null)
        return scorer
    }
}