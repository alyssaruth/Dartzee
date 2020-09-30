package dartzee.helper

import dartzee.`object`.Dart
import dartzee.`object`.DartboardSegment
import dartzee.`object`.SegmentType
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.dart.*
import dartzee.dartzee.getAllTotalRules
import dartzee.dartzee.total.AbstractDartzeeRuleTotalSize
import dartzee.dartzee.total.AbstractDartzeeTotalRule
import dartzee.dartzee.total.DartzeeTotalRuleEqualTo
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.ParticipantEntity
import dartzee.game.state.DartzeePlayerState
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.getAllPossibleSegments

val twoBlackOneWhite = makeDartzeeRuleDto(makeColourRule(black = true), makeColourRule(black = true), makeColourRule(white = true),
        inOrder = false,
        calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { it.getMultiplier() == 1 && it.score != 25 }))

val scoreEighteens = makeDartzeeRuleDto(makeScoreRule(18),
        calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { !it.isMiss() && it.score == 18 },
                getAllPossibleSegments().filter { !it.isMiss() }))

val innerOuterInner = makeDartzeeRuleDto(DartzeeDartRuleInner(), DartzeeDartRuleOuter(), DartzeeDartRuleInner(),
        inOrder = true,
        calculationResult = makeDartzeeRuleCalculationResult(getInnerSegments()))

val totalIsFifty = makeDartzeeRuleDto(totalRule = makeTotalScoreRule<DartzeeTotalRuleEqualTo>(50),
        calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { !it.isMiss() }))

val allTwenties = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(20), makeScoreRule(20),
        inOrder = true,
        calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { it.score == 20 && !it.isMiss() }))

fun makeDartzeeRuleDto(dart1Rule: AbstractDartzeeDartRule? = null,
                       dart2Rule: AbstractDartzeeDartRule? = null,
                       dart3Rule: AbstractDartzeeDartRule? = null,
                       totalRule: AbstractDartzeeTotalRule? = null,
                       inOrder: Boolean = false,
                       allowMisses: Boolean = false,
                       calculationResult: DartzeeRuleCalculationResult = makeDartzeeRuleCalculationResult()): DartzeeRuleDto
{
    val rule = DartzeeRuleDto(dart1Rule, dart2Rule, dart3Rule, totalRule, inOrder, allowMisses)
    rule.calculationResult = calculationResult
    return rule
}

fun makeDartzeeRuleCalculationResult(scoringSegments: List<DartboardSegment> = emptyList(),
                                     validSegments: List<DartboardSegment> = scoringSegments,
                                     validCombinations: Int = 10,
                                     allCombinations: Int = 50,
                                     validCombinationProbability: Double = 1.0,
                                     allCombinationsProbability: Double = 6.0): DartzeeRuleCalculationResult
{
    return DartzeeRuleCalculationResult(scoringSegments, validSegments, validCombinations, allCombinations, validCombinationProbability, allCombinationsProbability)
}

fun makeDartzeeRuleCalculationResult(percentage: Int): DartzeeRuleCalculationResult
{
    return DartzeeRuleCalculationResult(emptyList(), emptyList(), 10, 50, percentage.toDouble(), 100.toDouble())
}

fun makeScoreRule(score: Int) = DartzeeDartRuleScore().also { it.score = score }
fun makeColourRule(red: Boolean = false, green: Boolean = false, black: Boolean = false, white: Boolean = false): DartzeeDartRuleColour
{
    val rule = DartzeeDartRuleColour()
    rule.black = black
    rule.white = white
    rule.red = red
    rule.green = green
    return rule
}

inline fun <reified T: AbstractDartzeeRuleTotalSize> makeTotalScoreRule(score: Int) = getAllTotalRules().find { it is T }.also { (it as T).target = score }

fun getOuterSegments() = getAllPossibleSegments().filter { it.type == SegmentType.DOUBLE || it.type == SegmentType.OUTER_SINGLE }.filter { it.score != 25 }
fun getInnerSegments() = getAllPossibleSegments().filter { (it.score == 25 && !it.isMiss()) || it.type == SegmentType.TREBLE || it.type == SegmentType.INNER_SINGLE }

fun makeRoundResultEntities(vararg roundResult: DartzeeRoundResult): List<DartzeeRoundResultEntity> {
    val pt = insertParticipant()
    return roundResult.mapIndexed { index, result -> DartzeeRoundResultEntity.factoryAndSave(result, pt, index + 2) }
}

fun makeDartzeePlayerStateForName(name: String = "Bob",
                                  completedRounds: List<List<Dart>> = emptyList(),
                                  roundResults: List<DartzeeRoundResult> = emptyList()): DartzeePlayerState
{
    val p = insertPlayer(name = name)
    val pt = insertParticipant(playerId = p.rowId)
    return makeDartzeePlayerState(pt, completedRounds, roundResults)
}

fun makeDartzeePlayerState(participant: ParticipantEntity = insertParticipant(),
                           completedRounds: List<List<Dart>> = emptyList(),
                           roundResults: List<DartzeeRoundResult> = emptyList()): DartzeePlayerState
{
    val resultEntities = makeRoundResultEntities(*roundResults.toTypedArray())
    return DartzeePlayerState(participant, completedRounds.toMutableList(), mutableListOf(), false, resultEntities.toMutableList())
}

fun makeSegmentStatus(scoringSegments: List<DartboardSegment> = getAllPossibleSegments(),
                      validSegments: List<DartboardSegment> = scoringSegments)
 = SegmentStatus(scoringSegments, validSegments)