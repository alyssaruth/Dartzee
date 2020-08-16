package dartzee.helper

import dartzee.`object`.SegmentType
import dartzee.ai.AimDart
import dartzee.ai.DartsAiModel
import dartzee.ai.DartzeePlayStyle

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