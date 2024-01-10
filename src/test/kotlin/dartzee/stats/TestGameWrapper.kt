package dartzee.stats

import dartzee.helper.AbstractTest
import dartzee.helper.makeGameWrapper
import dartzee.helper.makeX01Rounds
import dartzee.`object`.Dart
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGameWrapper : AbstractTest() {
    @Test
    fun `Should capture darts accurately`() {
        val roundTwo = listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1))
        val rounds = makeX01Rounds(501, listOf(Dart(20, 1), Dart(20, 1), Dart(20, 3)), roundTwo)

        val gameWrapper = makeGameWrapper()
        rounds.flatten().forEach(gameWrapper::addDart)

        gameWrapper.getDartsForFinalRound() shouldBe roundTwo
        gameWrapper.getAllDarts().shouldContainExactly(rounds.flatten())
    }
}
