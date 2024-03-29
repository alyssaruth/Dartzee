package dartzee.ai

import dartzee.bean.PresentationDartboard
import dartzee.drtDoubleTwenty
import dartzee.drtMissTwenty
import dartzee.drtOuterEighteen
import dartzee.drtOuterFive
import dartzee.drtOuterOne
import dartzee.drtOuterTwelve
import dartzee.drtOuterTwenty
import dartzee.drtTrebleFive
import dartzee.drtTrebleOne
import dartzee.drtTrebleTwenty
import dartzee.helper.AbstractTest
import dartzee.helper.makeThrowDartFn
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.awt.Dimension
import org.junit.jupiter.api.Test

class TestDartsAiSimulator : AbstractTest() {
    @Test
    fun `Should return expected values for deterministic AI`() {
        val dartboard = PresentationDartboard().also { it.size = Dimension(100, 100) }

        val scoringDarts =
            (1..200).map { drtMissTwenty() } + // 1%
                (1..200).map { drtDoubleTwenty() } + // 1%
                (1..12000).map { drtOuterTwenty() } + // 60%
                (1..2000).map { drtTrebleTwenty() } + // 10%
                (1..1000).map { drtOuterOne() } + // 5%
                (1..1000).map { drtOuterFive() } + // 5%
                (1..1000).map { drtTrebleOne() } + // 5%
                (1..1000).map { drtTrebleFive() } + // 5%
                (1..800).map { drtOuterTwelve() } + // 4%
                (1..800).map { drtOuterEighteen() } // 4%

        val doubleSegmentTypes =
            (1..2000).map { SegmentType.DOUBLE } + // 10%
                (1..9000).map { SegmentType.MISS } + // 45%
                (1..9000).map { SegmentType.OUTER_SINGLE } // 45%

        val model = mockDartsModel(scoringDarts, doubleSegmentTypes)

        val result = DartsAiSimulator.runSimulation(model, dartboard)
        result.finishPercent shouldBe 10.0
        result.missPercent shouldBe 1.0
        result.treblePercent shouldBe 10.0
        result.averageDart shouldBe scoringDarts.map { it.getTotal() }.average()
    }

    private fun mockDartsModel(scoringDarts: List<Dart>, doubles: List<SegmentType>): DartsAiModel {
        val model = mockk<DartsAiModel>()
        every { model.scoringDart } returns 20
        every { model.calculateScoringPoint() } returns
            getComputedPointForScore(20, SegmentType.TREBLE)

        val aimDarts = scoringDarts.map { it.toAimDart() }.shuffled()
        val throwDartFn = makeThrowDartFn(aimDarts)
        every { model.throwScoringDart() } answers { throwDartFn() }

        val shuffledSegmentTypes = doubles.shuffled().toMutableList()
        val doubleSlot = slot<Int>()
        every { model.throwAtDouble(capture(doubleSlot)) } answers
            {
                val score = doubleSlot.captured
                val segmentType = shuffledSegmentTypes.removeFirst()
                if (segmentType == SegmentType.MISS) AI_DARTBOARD.getDeliberateMissPoint()
                else getComputedPointForScore(score, segmentType)
            }

        return model
    }
}
