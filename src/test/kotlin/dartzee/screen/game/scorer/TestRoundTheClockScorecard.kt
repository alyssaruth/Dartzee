package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.makeClockPlayerState
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestRoundTheClockScorecard: AbstractTest()
{
    @Test
    fun `Should have correct rows for empty state`()
    {
        val scorecard = RoundTheClockScorecard()
        scorecard.stateChanged(makeClockPlayerState(isActive = false), false)

        val expectedRows = (1..20).map { ClockResult(it, false, false) }.chunked(4)
        scorecard.getRows() shouldBe expectedRows
    }

    @Test
    fun `Should flag as current target if unpaused and active`()
    {
        val state = makeClockPlayerState(isActive = true)
        val scorecard = RoundTheClockScorecard()
        scorecard.stateChanged(state, false)
        scorecard.getValueAt(0, 0) shouldBe ClockResult(1, hit = false, isCurrentTarget = true)
    }

    @Test
    fun `Should not flag as current target if paused`()
    {
        val state = makeClockPlayerState(isActive = true)
        val scorecard = RoundTheClockScorecard()
        scorecard.stateChanged(state, true)
        scorecard.getValueAt(0, 0) shouldBe ClockResult(1, hit = false, isCurrentTarget = false)
    }

    @Test
    fun `Should flag as current target if inactive`()
    {
        val state = makeClockPlayerState(isActive = false)
        val scorecard = RoundTheClockScorecard()
        scorecard.stateChanged(state, false)
        scorecard.getValueAt(0, 0) shouldBe ClockResult(1, hit = false, isCurrentTarget = false)
    }

    @Test
    fun `Should flag results that have been hit`()
    {
        val state = makeClockPlayerState(isActive = false, inOrder = false)
        state.dartThrown(Dart(1, 1))
        state.dartThrown(Dart(4, 1))

        val scorecard = RoundTheClockScorecard()
        scorecard.stateChanged(state, false)
        scorecard.getRows().first().shouldContainExactly(
            makeClockResult(1, hit = true),
            makeClockResult(2, hit = false),
            makeClockResult(3, hit = false),
            makeClockResult(4, hit = true)
        )
    }
}

fun makeClockResult(value: Int = 1, hit: Boolean = false, isCurrentTarget: Boolean = false) =
    ClockResult(value, hit, isCurrentTarget)