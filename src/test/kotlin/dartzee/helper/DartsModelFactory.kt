package dartzee.helper

import dartzee.ai.AimDart
import dartzee.ai.DartsAiModel
import dartzee.ai.DartzeePlayStyle
import dartzee.`object`.SegmentType
import getPointForScore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.awt.Point

fun beastDartsModel(standardDeviationDoubles: Double? = null,
                    standardDeviationCentral: Double? = null,
                    maxRadius: Int = 450,
                    scoringDart: Int = 20,
                    hmScoreToDart: Map<Int, AimDart> = emptyMap(),
                    mercyThreshold: Int? = null,
                    hmDartNoToSegmentType: Map<Int, SegmentType> = DartsAiModel.DEFAULT_GOLF_SEGMENT_TYPES.toMutableMap(),
                    hmDartNoToStopThreshold: Map<Int, Int> = DartsAiModel.DEFAULT_GOLF_STOP_THRESHOLDS.toMutableMap(),
                    dartzeePlayStyle: DartzeePlayStyle = DartzeePlayStyle.CAUTIOUS): DartsAiModel
{
    return DartsAiModel(
            0.1,
            standardDeviationDoubles,
            standardDeviationCentral,
            maxRadius,
            scoringDart,
            hmScoreToDart,
            mercyThreshold,
            hmDartNoToSegmentType,
            hmDartNoToStopThreshold,
            dartzeePlayStyle)
}

fun makeDartsModel(standardDeviation: Double = 50.0,
                   standardDeviationDoubles: Double? = null,
                   standardDeviationCentral: Double? = null,
                   maxRadius: Int = 250,
                   scoringDart: Int = 20,
                   hmScoreToDart: Map<Int, AimDart> = emptyMap(),
                   mercyThreshold: Int? = null,
                   hmDartNoToSegmentType: Map<Int, SegmentType> = DartsAiModel.DEFAULT_GOLF_SEGMENT_TYPES.toMutableMap(),
                   hmDartNoToStopThreshold: Map<Int, Int> = DartsAiModel.DEFAULT_GOLF_STOP_THRESHOLDS.toMutableMap(),
                   dartzeePlayStyle: DartzeePlayStyle = DartzeePlayStyle.CAUTIOUS): DartsAiModel
{
    return DartsAiModel(
            standardDeviation,
            standardDeviationDoubles,
            standardDeviationCentral,
            maxRadius,
            scoringDart,
            hmScoreToDart,
            mercyThreshold,
            hmDartNoToSegmentType,
            hmDartNoToStopThreshold,
            dartzeePlayStyle)
}

fun predictableDartsModel(
    dartsToThrow: List<AimDart>,
    dartzeePlayStyle: DartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE,
    mercyThreshold: Int? = null,
    golfStopThresholds: Map<Int, Int>? = null,
): DartsAiModel
{
    val defaultModel = DartsAiModel.new()
    val hmDartNoToStopThreshold = golfStopThresholds ?: defaultModel.hmDartNoToStopThreshold

    val model = mockk<DartsAiModel>()
    every { model.mercyThreshold } returns mercyThreshold
    every { model.hmDartNoToStopThreshold } returns hmDartNoToStopThreshold
    every { model.dartzeePlayStyle } returns dartzeePlayStyle

    val throwDartFn = makeThrowDartFn(dartsToThrow)

    every { model.throwScoringDart() } answers { throwDartFn() }
    every { model.throwDartzeeDart(any(), any()) } answers { throwDartFn() }
    every { model.throwX01Dart(any()) } answers { throwDartFn() }
    every { model.throwClockDart(any(), any()) } answers { throwDartFn() }
    every { model.throwGolfDart(any(), any()) } answers { throwDartFn() }
    every { model.getStopThresholdForDartNo(any()) } answers { callOriginal() }
    return model
}

fun makeThrowDartFn(dartsToThrow: List<AimDart>): () -> Point
{
    val remainingDarts = dartsToThrow.toMutableList()
    val throwDartFn = {
        val dart = remainingDarts.removeAt(0)
        getPointForScore(dart)
    }

    return throwDartFn
}

data class ScoreAndSegmentType(val score: Int, val segmentType: SegmentType)
fun predictableGolfModel(hmDartNoToStopThreshold: Map<Int, Int> = DartsAiModel.DEFAULT_GOLF_STOP_THRESHOLDS.toMutableMap(), fn: (hole: Int, dartNo: Int) -> ScoreAndSegmentType): DartsAiModel
{
    val model = mockk<DartsAiModel>(relaxed = true)
    val stopThresholdSlot = slot<Int>()
    every { model.getStopThresholdForDartNo(capture(stopThresholdSlot)) } answers { hmDartNoToStopThreshold.getValue(stopThresholdSlot.captured) }

    val holeSlot = slot<Int>()
    val dartNoSlot = slot<Int>()
    every { model.throwGolfDart(capture(holeSlot), capture(dartNoSlot)) } answers {
        val result = fn(holeSlot.captured, dartNoSlot.captured)
        getPointForScore(result.score, result.segmentType)
    }

    return model
}