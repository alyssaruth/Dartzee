package burlton.dartzee.test.screen.dartzee

import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_MISS
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.dartzee.DartzeeCalculator
import burlton.dartzee.code.dartzee.DartzeeRoundResult
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleInner
import burlton.dartzee.code.dartzee.dart.DartzeeDartRuleOuter
import burlton.dartzee.code.dartzee.total.DartzeeTotalRuleEqualTo
import burlton.dartzee.code.db.DartzeeRoundResultEntity
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCarousel
import burlton.dartzee.code.screen.dartzee.DartzeeRuleTile
import burlton.dartzee.code.screen.dartzee.IDartzeeCarouselListener
import burlton.dartzee.code.utils.InjectedThings
import burlton.dartzee.code.utils.getAllPossibleSegments
import burlton.dartzee.test.helper.*
import burlton.desktopcore.code.util.getAllChildComponentsForType
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import java.awt.Color

class TestDartzeeRuleCarousel: AbstractDartsTest()
{
    val twoBlackOneWhite = makeDartzeeRuleDto(makeColourRule(black = true), makeColourRule(black = true), makeColourRule(white = true),
            inOrder = false,
            calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { it.getMultiplier() == 1 }))

    val scoreEighteens = makeDartzeeRuleDto(makeScoreRule(18), calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { !it.isMiss() }))

    val innerOuterInner = makeDartzeeRuleDto(DartzeeDartRuleInner(), DartzeeDartRuleOuter(), DartzeeDartRuleInner(),
            inOrder = true,
            calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { !it.isMiss() }))

    val totalIsFifty = makeDartzeeRuleDto(totalRule = makeTotalScoreRule<DartzeeTotalRuleEqualTo>(50), calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { !it.isMiss() }))
    val allTwenties = makeDartzeeRuleDto(makeScoreRule(20), makeScoreRule(20), makeScoreRule(20),
            inOrder = true,
            calculationResult = makeDartzeeRuleCalculationResult(getAllPossibleSegments().filter { it.score == 20 && !it.isMiss() }))

    val dtos = listOf(twoBlackOneWhite, scoreEighteens, innerOuterInner, totalIsFifty, allTwenties)

    override fun afterEachTest()
    {
        super.afterEachTest()

        InjectedThings.dartzeeCalculator = FakeDartzeeCalculator()
    }

    @Test
    fun `Should display the correct initial state`()
    {
        val carousel = makeCarousel()
        carousel.update(emptyList(), emptyList(), 20)

        carousel.dartsThrown.shouldBeEmpty()
        carousel.completeTiles.shouldBeEmpty()
        carousel.pendingTiles.map { it.dto }.shouldContainAll(dtos)
        carousel.pendingTiles.filter { it.pendingResult != null }.shouldBeEmpty()
        carousel.toggleButtonComplete.isEnabled shouldBe false
        carousel.toggleButtonComplete.isSelected shouldBe false
        carousel.toggleButtonPending.isSelected shouldBe true
    }

    @Test
    fun `Should remove pending rules that are invalid based on darts thrown`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val carousel = makeCarousel()
        val dart = makeDart(18, 1, SEGMENT_TYPE_OUTER_SINGLE)
        carousel.update(emptyList(), listOf(dart), 20)

        carousel.dartsThrown.shouldContainExactly(dart)
        carousel.completeTiles.shouldBeEmpty()
        carousel.getPendingRules().shouldContainExactly(twoBlackOneWhite, scoreEighteens, totalIsFifty)
    }

    @Test
    fun `Should use round results to separate pending and completed rules`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val carousel = makeCarousel()

        val results = makeRoundResultEntities(DartzeeRoundResult(3, true, 36), DartzeeRoundResult(1, false, -38))
        carousel.update(results, emptyList(), 38)

        carousel.getPendingRules().shouldContainExactly(scoreEighteens, totalIsFifty, allTwenties)
        carousel.toggleButtonComplete.isEnabled shouldBe true

        val completeTiles = carousel.completeTiles
        completeTiles.size shouldBe 2

        completeTiles[0].dto shouldBe innerOuterInner
        completeTiles[0].getScore() shouldBe 36
        completeTiles[0].background shouldBe Color.GREEN

        completeTiles[1].dto shouldBe twoBlackOneWhite
        completeTiles[1].getScore() shouldBe -38
        completeTiles[1].background shouldBe Color.RED
    }

    @Test
    fun `If three darts have been thrown, any remaining rules should get a pending result of success`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val carousel = makeCarousel()
        val darts = listOf(makeDart(18, 1, SEGMENT_TYPE_INNER_SINGLE),
                makeDart(20, 1, SEGMENT_TYPE_OUTER_SINGLE),
                makeDart(1, 1, SEGMENT_TYPE_INNER_SINGLE))

        carousel.update(emptyList(), darts, 50)

        carousel.getPendingRules().shouldContainExactly(twoBlackOneWhite, scoreEighteens, innerOuterInner)
        val pendingTiles = carousel.pendingTiles.filter { it.isVisible }
        pendingTiles.forEach {
            it.pendingResult shouldBe true
            it.pendingScore shouldBe it.dto.getSuccessTotal(darts)
        }
    }

    @Test
    fun `If no remaining rules are valid, then the first should be shown with a pending result of false`()
    {
        val carousel = makeCarousel()
        carousel.update(emptyList(), listOf(makeDart(20, 0, SEGMENT_TYPE_MISS)), 20)

        val pendingTiles = carousel.pendingTiles.filter { it.isVisible }
        pendingTiles.size shouldBe 1

        val pendingTile = pendingTiles.first()
        pendingTile.dto shouldBe twoBlackOneWhite
        pendingTile.pendingResult shouldBe false
        pendingTile.pendingScore shouldBe -10
    }

    @Test
    fun `Should round up when calculating a failure score`()
    {
        val carousel = makeCarousel()
        carousel.update(emptyList(), listOf(makeDart(20, 0, SEGMENT_TYPE_MISS)), 41)

        val pendingTiles = carousel.pendingTiles.filter { it.isVisible }
        pendingTiles.size shouldBe 1

        val pendingTile = pendingTiles.first()
        pendingTile.pendingScore shouldBe -20
    }

    @Test
    fun `Should return the union of valid segments based on pending rules`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val dartOne = makeDart(19, 1, SEGMENT_TYPE_OUTER_SINGLE)
        val dartTwo = makeDart(19, 1, SEGMENT_TYPE_OUTER_SINGLE)

        val carousel = makeCarousel()
        carousel.update(emptyList(), listOf(dartOne, dartTwo), 20)

        carousel.dartsThrown.shouldContainExactly(dartOne, dartTwo)
        carousel.getPendingRules().shouldContainExactly(scoreEighteens, totalIsFifty)

        val allSegments = getAllPossibleSegments()
        val eighteens = allSegments.filter { it.score == 18 && !it.isMiss() }
        val allTwelves = allSegments.filter { it.getTotal() == 12 }
        carousel.getValidSegments().shouldContainAll(eighteens + allTwelves)
    }

    @Test
    fun `Should toggle between pending and complete tiles`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val carousel = makeCarousel()

        val results = makeRoundResultEntities(DartzeeRoundResult(3, true, 36), DartzeeRoundResult(1, false, -38))
        carousel.update(results, emptyList(), 38)

        carousel.getDisplayedRules().shouldContainExactly(scoreEighteens, totalIsFifty, allTwenties)

        carousel.toggleButtonComplete.doClick()
        carousel.getDisplayedRules().shouldContainExactly(innerOuterInner, twoBlackOneWhite)

        carousel.toggleButtonPending.doClick()
        carousel.getDisplayedRules().shouldContainExactly(scoreEighteens, totalIsFifty, allTwenties)
    }

    private fun DartzeeRuleCarousel.getDisplayedRules() = getAllChildComponentsForType(tilePanel, DartzeeRuleTile::class.java).map { it.dto }
    private fun makeCarousel(listener: IDartzeeCarouselListener = mockk(relaxed = true)) = DartzeeRuleCarousel(listener, dtos)
    private fun DartzeeRuleCarousel.getPendingRules() = pendingTiles.filter { it.isVisible }.map { it.dto }
    private fun makeRoundResultEntities(vararg roundResult: DartzeeRoundResult): List<DartzeeRoundResultEntity> {
        val pt = insertParticipant()
        return roundResult.mapIndexed { index, result -> DartzeeRoundResultEntity.factoryAndSave(result, pt, index + 1) }
    }
}