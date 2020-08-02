package dartzee.helper

import dartzee.`object`.SegmentType
import dartzee.ai.AimDart
import dartzee.ai.DartsAiModelMk2
import dartzee.ai.DartzeePlayStyle

fun beastDartsModel(standardDeviationDoubles: Double? = null,
                    standardDeviationCentral: Double? = null,
                    scoringDart: Int = 20,
                    hmScoreToDart: Map<Int, AimDart> = emptyMap(),
                    mercyThreshold: Int? = null,
                    hmDartNoToSegmentType: Map<Int, SegmentType> = emptyMap(),
                    hmDartNoToStopThreshold: Map<Int, Int> = emptyMap(),
                    dartzeePlayStyle: DartzeePlayStyle = DartzeePlayStyle.CAUTIOUS): DartsAiModelMk2
{
    return DartsAiModelMk2(
            0.1,
            standardDeviationDoubles,
            standardDeviationCentral,
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
                   scoringDart: Int = 20,
                   hmScoreToDart: Map<Int, AimDart> = emptyMap(),
                   mercyThreshold: Int? = null,
                   hmDartNoToSegmentType: Map<Int, SegmentType> = emptyMap(),
                   hmDartNoToStopThreshold: Map<Int, Int> = emptyMap(),
                   dartzeePlayStyle: DartzeePlayStyle = DartzeePlayStyle.CAUTIOUS): DartsAiModelMk2
{
    return DartsAiModelMk2(
            standardDeviation,
            standardDeviationDoubles,
            standardDeviationCentral,
            scoringDart,
            hmScoreToDart,
            mercyThreshold,
            hmDartNoToSegmentType,
            hmDartNoToStopThreshold,
            dartzeePlayStyle)
}