package dartzee.helper

import com.github.alyssaburlton.swingtest.toBufferedImage
import dartzee.bean.PresentationDartboard
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.aggregate.AbstractDartzeeAggregateRule
import dartzee.dartzee.aggregate.AbstractDartzeeRuleTotalSize
import dartzee.dartzee.aggregate.DartzeeTotalRuleEqualTo
import dartzee.dartzee.dart.AbstractDartzeeDartRule
import dartzee.dartzee.dart.DartzeeDartRuleColour
import dartzee.dartzee.dart.DartzeeDartRuleInner
import dartzee.dartzee.dart.DartzeeDartRuleOuter
import dartzee.dartzee.dart.DartzeeDartRuleScore
import dartzee.dartzee.getAllAggregateRules
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.ParticipantEntity
import dartzee.game.state.DartzeePlayerState
import dartzee.game.state.SingleParticipant
import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.screen.game.SegmentStatuses
import dartzee.utils.getAllNonMissSegments
import dartzee.utils.getAllPossibleSegments
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Point
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel

val twoBlackOneWhite =
    makeDartzeeRuleDto(
        makeColourRule(black = true),
        makeColourRule(black = true),
        makeColourRule(white = true),
        inOrder = false,
        calculationResult =
            makeDartzeeRuleCalculationResult(
                getAllNonMissSegments().filter { it.getMultiplier() == 1 && it.score != 25 }
            )
    )

val scoreEighteens =
    makeDartzeeRuleDto(
        makeScoreRule(18),
        calculationResult =
            makeDartzeeRuleCalculationResult(
                getAllNonMissSegments().filter { it.score == 18 },
                getAllNonMissSegments()
            )
    )

val innerOuterInner =
    makeDartzeeRuleDto(
        DartzeeDartRuleInner(),
        DartzeeDartRuleOuter(),
        DartzeeDartRuleInner(),
        inOrder = true,
        calculationResult = makeDartzeeRuleCalculationResult(getInnerSegments())
    )

val totalIsFifty =
    makeDartzeeRuleDto(
        aggregateRule = makeTotalScoreRule<DartzeeTotalRuleEqualTo>(50),
        calculationResult = makeDartzeeRuleCalculationResult(getAllNonMissSegments())
    )

val allTwenties =
    makeDartzeeRuleDto(
        makeScoreRule(20),
        makeScoreRule(20),
        makeScoreRule(20),
        inOrder = true,
        calculationResult =
            makeDartzeeRuleCalculationResult(getAllNonMissSegments().filter { it.score == 20 })
    )

val testRules = listOf(twoBlackOneWhite, innerOuterInner, scoreEighteens, totalIsFifty)

fun makeDartzeeRuleDto(
    dart1Rule: AbstractDartzeeDartRule? = null,
    dart2Rule: AbstractDartzeeDartRule? = null,
    dart3Rule: AbstractDartzeeDartRule? = null,
    aggregateRule: AbstractDartzeeAggregateRule? = null,
    inOrder: Boolean = false,
    allowMisses: Boolean = false,
    calculationResult: DartzeeRuleCalculationResult = makeDartzeeRuleCalculationResult(),
    ruleName: String? = null
): DartzeeRuleDto {
    val rule =
        DartzeeRuleDto(
            dart1Rule,
            dart2Rule,
            dart3Rule,
            aggregateRule,
            inOrder,
            allowMisses,
            ruleName
        )
    rule.calculationResult = calculationResult
    return rule
}

fun makeDartzeeRuleCalculationResult(
    scoringSegments: List<DartboardSegment> = emptyList(),
    validSegments: List<DartboardSegment> = scoringSegments,
    validCombinations: Int = 10,
    allCombinations: Int = 50,
    validCombinationProbability: Double = 1.0,
    allCombinationsProbability: Double = 6.0
) =
    DartzeeRuleCalculationResult(
        scoringSegments,
        validSegments,
        validCombinations,
        allCombinations,
        validCombinationProbability,
        allCombinationsProbability
    )

fun makeDartzeeRuleCalculationResult(percentage: Int) =
    DartzeeRuleCalculationResult(
        emptyList(),
        emptyList(),
        10,
        50,
        percentage.toDouble(),
        100.toDouble()
    )

fun makeScoreRule(score: Int) = DartzeeDartRuleScore().also { it.score = score }

fun makeColourRule(
    red: Boolean = false,
    green: Boolean = false,
    black: Boolean = false,
    white: Boolean = false
): DartzeeDartRuleColour {
    val rule = DartzeeDartRuleColour()
    rule.black = black
    rule.white = white
    rule.red = red
    rule.green = green
    return rule
}

inline fun <reified T : AbstractDartzeeRuleTotalSize> makeTotalScoreRule(score: Int) =
    getAllAggregateRules().find { it is T }.also { (it as T).target = score }

fun getOuterSegments() =
    getAllNonMissSegments()
        .filter { it.type == SegmentType.DOUBLE || it.type == SegmentType.OUTER_SINGLE }
        .filter { it.score != 25 }

fun getInnerSegments() =
    getAllNonMissSegments().filter {
        it.score == 25 || it.type == SegmentType.TREBLE || it.type == SegmentType.INNER_SINGLE
    }

fun makeRoundResultEntities(
    vararg roundResult: DartzeeRoundResult
): List<DartzeeRoundResultEntity> {
    val pt = insertParticipant()
    return roundResult.mapIndexed { index, result ->
        DartzeeRoundResultEntity.factoryAndSave(result, pt, index + 2)
    }
}

fun makeDartzeePlayerStateForName(
    name: String = "Bob",
    completedRounds: List<List<Dart>> = emptyList(),
    roundResults: List<DartzeeRoundResult> = emptyList()
): DartzeePlayerState {
    val p = insertPlayer(name = name)
    val pt = insertParticipant(playerId = p.rowId)
    return makeDartzeePlayerState(pt, completedRounds, roundResults)
}

fun makeDartzeePlayerState(
    participant: ParticipantEntity = insertParticipant(),
    completedRounds: List<List<Dart>> = emptyList(),
    roundResults: List<DartzeeRoundResult> = emptyList()
): DartzeePlayerState {
    val resultEntities = makeRoundResultEntities(*roundResults.toTypedArray())
    return DartzeePlayerState(
        SingleParticipant(participant),
        completedRounds.toMutableList(),
        mutableListOf(),
        false,
        resultEntities.toMutableList()
    )
}

fun makeSegmentStatuses(
    scoringSegments: List<DartboardSegment> = getAllPossibleSegments(),
    validSegments: List<DartboardSegment> = scoringSegments
) = SegmentStatuses(scoringSegments, validSegments)

fun PresentationDartboard.markPoints(points: List<Point>) = markPoints(toBufferedImage(), points)

private fun markPoints(img: BufferedImage, points: List<Point>): JLabel {
    val g = img.graphics as Graphics2D
    g.color = Color.BLUE
    g.stroke = BasicStroke(3f)
    points.forEach { pt ->
        g.drawLine(pt.x - 5, pt.y - 5, pt.x + 5, pt.y + 5)
        g.drawLine(pt.x - 5, pt.y + 5, pt.x + 5, pt.y - 5)
    }

    val lbl = JLabel(ImageIcon(img))
    lbl.size = Dimension(500, 500)
    lbl.repaint()
    return lbl
}
