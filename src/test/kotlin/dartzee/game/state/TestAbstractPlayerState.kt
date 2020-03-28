package dartzee.game.state

import dartzee.`object`.Dart
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.screen.game.scorer.DartsScorer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import org.junit.Test

class TestAbstractPlayerState: AbstractTest()
{
    @Test
    fun `it should take a copy of the darts that are added`()
    {
        val state = DefaultPlayerState(insertParticipant(), mockk<DartsScorer>())
        val darts = mutableListOf(Dart(20, 1))

        state.addDarts(darts)

        darts.clear()
        state.darts.first().shouldContainExactly(Dart(20, 1))
    }
}
