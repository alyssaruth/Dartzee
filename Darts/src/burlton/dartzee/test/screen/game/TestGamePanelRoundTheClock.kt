package burlton.dartzee.test.screen.game

import burlton.core.code.obj.HashMapList
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.db.CLOCK_TYPE_STANDARD
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.game.DartsGameScreen
import burlton.dartzee.code.screen.game.DartsScorerRoundTheClock
import burlton.dartzee.code.screen.game.GamePanelRoundTheClock
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestGamePanelRoundTheClock: AbstractDartsTest()
{
    @Test
    fun `Should load a current streak across several rounds`()
    {
        val panel = TestRoundTheClockGamePanel()

        val roundOne = mutableListOf(Dart(20, 1), factoryClockHit(1), factoryClockHit(2))
        val roundTwo = mutableListOf(factoryClockHit(3), factoryClockHit(4), factoryClockHit(5), factoryClockHit(6))

        val hm = HashMapList<Int, Dart>()
        hm[1] = roundOne
        hm[2] = roundTwo

        panel.loadDartsForParticipant(0, hm, 2)

        panel.hmPlayerNumberToCurrentStreak[0] shouldBe 6
    }

    @Test
    fun `Should load a current streak of 0 if the last dart was a miss`()
    {
        val panel = TestRoundTheClockGamePanel()

        val roundOne = mutableListOf(factoryClockHit(1), factoryClockHit(2), Dart(5, 1))
        val hm = HashMapList<Int, Dart>()
        hm[1] = roundOne

        panel.loadDartsForParticipant(0, hm, 1)

        panel.hmPlayerNumberToCurrentStreak[0] shouldBe 0
    }

    private fun factoryClockHit(clockTarget: Int): Dart
    {
        val dart = Dart(clockTarget, 1)
        dart.startingScore = clockTarget
        return dart
    }

    class TestRoundTheClockGamePanel: GamePanelRoundTheClock(DartsGameScreen())
    {
        init
        {
            for (i in 0..3)
            {
                val scorer = DartsScorerRoundTheClock()
                scorer.init(PlayerEntity(), CLOCK_TYPE_STANDARD)
                hmPlayerNumberToDartsScorer[i] = scorer
            }
        }
    }
}