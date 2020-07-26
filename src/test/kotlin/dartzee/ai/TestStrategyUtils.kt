package dartzee.ai

import dartzee.`object`.Dart
import dartzee.helper.AbstractTest
import getPointForScore
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.Point

class TestStrategyUtils: AbstractTest()
{
    @Test
    fun `Should aim at the average point for the relevant segment`()
    {
        val dartboard = TestDartsAiModel.FudgedDartboard()
        dartboard.paintDartboard()

        getPointForScore(Dart(20, 3), dartboard) shouldBe Point(3, 4)
    }
}