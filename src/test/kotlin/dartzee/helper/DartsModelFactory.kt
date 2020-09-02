package dartzee.helper

import dartzee.`object`.SegmentType
import dartzee.ai.AimDart
import dartzee.ai.DartsAiModel
import dartzee.ai.DartzeePlayStyle
import dartzee.screen.Dartboard
import getPointForScore
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

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


fun predictableDartsModel(dartboard: Dartboard, mercyThreshold: Int? = null, fn: (startingScore: Int, dartsThrown: Int) -> AimDart): DartsAiModel
{
    val model = mockk<DartsAiModel>(relaxed = true)
    every { model.mercyThreshold } returns mercyThreshold

    var dartsThrown = 0
    val slot = slot<Int>()
    every { model.throwX01Dart(capture(slot), any()) } answers {
        val aimDart = fn(slot.captured, dartsThrown)
        val pt = getPointForScore(aimDart, dartboard)
        dartboard.dartThrown(pt)
        dartsThrown++
    }

    return model
}