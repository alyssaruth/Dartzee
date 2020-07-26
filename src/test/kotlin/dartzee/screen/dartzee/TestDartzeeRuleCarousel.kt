package dartzee.screen.dartzee

import dartzee.`object`.SegmentType
import dartzee.ai.DartsAiModel
import dartzee.ai.DartzeePlayStyle
import dartzee.core.helper.makeMouseEvent
import dartzee.core.helper.verifyNotCalled
import dartzee.core.util.getAllChildComponentsForType
import dartzee.dartzee.DartzeeCalculator
import dartzee.dartzee.DartzeeRoundResult
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.DartzeeRoundResultEntity
import dartzee.helper.*
import dartzee.screen.game.dartzee.DartzeeRuleCarousel
import dartzee.screen.game.dartzee.DartzeeRuleTile
import dartzee.screen.game.dartzee.IDartzeeCarouselListener
import dartzee.screen.game.dartzee.SegmentStatus
import dartzee.utils.InjectedThings
import dartzee.utils.getAllPossibleSegments
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.awt.Color
import java.util.concurrent.locks.ReentrantLock

class TestDartzeeRuleCarousel: AbstractTest()
{
    private val dtos = listOf(twoBlackOneWhite, scoreEighteens, innerOuterInner, totalIsFifty, allTwenties)

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
        val dart = makeDart(18, 1, SegmentType.OUTER_SINGLE)
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
        completeTiles[0].getScoreForHover() shouldBe 36
        completeTiles[0].background shouldBe Color.GREEN

        completeTiles[1].dto shouldBe twoBlackOneWhite
        completeTiles[1].getScoreForHover() shouldBe -38
        completeTiles[1].background shouldBe Color.RED
    }

    @Test
    fun `If three darts have been thrown, any remaining rules should get a pending result of success`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val carousel = makeCarousel()
        val darts = listOf(makeDart(18, 1, SegmentType.INNER_SINGLE),
                makeDart(20, 1, SegmentType.OUTER_SINGLE),
                makeDart(1, 1, SegmentType.INNER_SINGLE))

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
        carousel.update(emptyList(), listOf(makeDart(20, 0, SegmentType.MISS)), 20)

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
        carousel.update(emptyList(), listOf(makeDart(20, 0, SegmentType.MISS)), 41)

        val pendingTiles = carousel.pendingTiles.filter { it.isVisible }
        pendingTiles.size shouldBe 1

        val pendingTile = pendingTiles.first()
        pendingTile.pendingScore shouldBe -20
    }

    @Test
    fun `Should return the union of valid segments based on pending rules`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val dartOne = makeDart(19, 1, SegmentType.OUTER_SINGLE)
        val dartTwo = makeDart(19, 1, SegmentType.OUTER_SINGLE)

        val carousel = makeCarousel()
        carousel.update(emptyList(), listOf(dartOne, dartTwo), 20)

        carousel.dartsThrown.shouldContainExactly(dartOne, dartTwo)
        carousel.getPendingRules().shouldContainExactly(scoreEighteens, totalIsFifty)

        val allSegments = getAllPossibleSegments()
        val eighteens = allSegments.filter { it.score == 18 && !it.isMiss() }
        val allTwelves = allSegments.filter { it.getTotal() == 12 }
        carousel.getSegmentStatus().validSegments.shouldContainAll(eighteens + allTwelves)
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

    @Test
    fun `Should hide pending tiles and toggle buttons on gameFinished`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val carousel = makeCarousel()

        val results = makeRoundResultEntities(DartzeeRoundResult(3, true, 36), DartzeeRoundResult(1, false, -38))
        carousel.update(results, emptyList(), 38)

        carousel.gameFinished()

        carousel.toggleButtonComplete.isSelected shouldBe true
        carousel.toggleButtonPanel.isVisible shouldBe false
        carousel.getDisplayedRules().shouldContainExactly(innerOuterInner, twoBlackOneWhite)
    }

    @Test
    fun `Clicking a tile with no pending result should have no effect`()
    {
        val listener = mockk<IDartzeeCarouselListener>(relaxed = true)
        val carousel = makeCarousel(listener)

        carousel.update(emptyList(), emptyList(), 50)

        carousel.pendingTiles.forEach {
            it.doClick()
        }

        verifyNotCalled { listener.tilePressed(any()) }
    }

    @Test
    fun `Clicking a tile with a pendingResult should notify the listener`()
    {
        val listener = mockk<IDartzeeCarouselListener>(relaxed = true)
        val carousel = makeCarousel(listener)
        carousel.update(emptyList(), listOf(makeDart(20, 0, SegmentType.MISS)), 20)

        val pendingTiles = carousel.pendingTiles.filter { it.isVisible }
        pendingTiles.size shouldBe 1

        val pendingTile = pendingTiles.first()
        pendingTile.doClick()

        verify { listener.tilePressed(DartzeeRoundResult(1, false, -10)) }
    }

    @Test
    fun `Hovering over a pending tile should notify the listener of that rule's valid segments`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val dartOne = makeDart(19, 1, SegmentType.OUTER_SINGLE)
        val dartTwo = makeDart(19, 1, SegmentType.OUTER_SINGLE)

        val listener = TrackingCarouselListener()
        val carousel = makeCarousel(listener)
        carousel.update(emptyList(), listOf(dartOne, dartTwo), 20)

        carousel.getDisplayedRules().shouldContainExactly(scoreEighteens, totalIsFifty)

        val allSegments = getAllPossibleSegments()
        val eighteens = allSegments.filter { it.score == 18 && !it.isMiss() }
        val allTwelves = allSegments.filter { it.getTotal() == 12 }

        val eighteensTile = carousel.getDisplayedTiles().find { it.dto == scoreEighteens }!!
        val me = makeMouseEvent(component = eighteensTile)
        carousel.mouseEntered(me)
        listener.segmentStatus.validSegments.shouldContainExactlyInAnyOrder(eighteens)

        val totalFiftyTile = carousel.getDisplayedTiles().find { it.dto == totalIsFifty }!!
        val meTotalFifty = makeMouseEvent(component = totalFiftyTile)
        carousel.mouseEntered(meTotalFifty)
        listener.segmentStatus.validSegments.shouldContainExactlyInAnyOrder(allTwelves)

        carousel.mouseExited(makeMouseEvent())
        listener.segmentStatus.validSegments.shouldContainExactlyInAnyOrder(eighteens + allTwelves)
    }

    @Test
    fun `Should correctly report whether or not it is initialised`()
    {
        val lock = ReentrantLock()

        // Mock up a result which will block until we release our hold on the lock
        val result = mockk<DartzeeRoundResultEntity>(relaxed = true)
        every { result.ruleNumber } answers {
            lock.lock()
            1
        }

        // Initial value should be false
        val carousel = makeCarousel()
        carousel.initialised shouldBe false

        // Prepare a thread to update
        val updateRunnable = Runnable { carousel.update(listOf(result), emptyList(), 20) }
        val updateThread = Thread(updateRunnable)

        lock.lock()
        updateThread.start()

        carousel.initialised shouldBe false
        Thread.sleep(1000)
        carousel.initialised shouldBe false

        // Let the other thread go and wait for it to complete. We should see initialised update immediately.
        lock.unlock()
        updateThread.join()
        carousel.initialised shouldBe true
    }

    @Test
    fun `Should select the 'hardest' rule for a cautious AI`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val dtos = listOf(twoBlackOneWhite, innerOuterInner, scoreEighteens)

        val listener = TrackingCarouselListener()

        val carousel = makeCarousel(listener, dtos)
        val darts = listOf(makeDart(18, 1, SegmentType.INNER_SINGLE),
                makeDart(20, 1, SegmentType.OUTER_SINGLE),
                makeDart(1, 1, SegmentType.INNER_SINGLE))

        carousel.update(emptyList(), darts, 50)
        carousel.getPendingRules().shouldContainExactly(twoBlackOneWhite, innerOuterInner, scoreEighteens)

        val ai = DartsAiModel()
        ai.dartzeePlayStyle = DartzeePlayStyle.CAUTIOUS
        carousel.selectRule(ai)

        val result = listener.roundResult!!
        result.ruleNumber shouldBe 3
        result.score shouldBe 18
        result.success shouldBe true
    }

    @Test
    fun `Should select the highest scoring hardest rule for an aggressive AI`()
    {
        InjectedThings.dartzeeCalculator = DartzeeCalculator()

        val dtos = listOf(twoBlackOneWhite, innerOuterInner, scoreEighteens)

        val listener = TrackingCarouselListener()

        val carousel = makeCarousel(listener, dtos)
        val darts = listOf(makeDart(18, 1, SegmentType.INNER_SINGLE),
                makeDart(20, 1, SegmentType.OUTER_SINGLE),
                makeDart(1, 1, SegmentType.INNER_SINGLE))

        carousel.update(emptyList(), darts, 50)
        carousel.getPendingRules().shouldContainExactly(twoBlackOneWhite, innerOuterInner, scoreEighteens)

        val ai = DartsAiModel()
        ai.dartzeePlayStyle = DartzeePlayStyle.AGGRESSIVE
        carousel.selectRule(ai)

        val result = listener.roundResult!!
        result.ruleNumber shouldBe 2
        result.score shouldBe 39
        result.success shouldBe true
    }

    private class TrackingCarouselListener: IDartzeeCarouselListener
    {
        var segmentStatus: SegmentStatus = SegmentStatus(emptySet(), emptySet())
        var roundResult: DartzeeRoundResult? = null

        override fun hoverChanged(segmentStatus: SegmentStatus)
        {
            this.segmentStatus = segmentStatus
        }

        override fun tilePressed(dartzeeRoundResult: DartzeeRoundResult)
        {
            this.roundResult = dartzeeRoundResult
        }
    }

    private fun DartzeeRuleCarousel.getDisplayedTiles() = tilePanel.getAllChildComponentsForType<DartzeeRuleTile>().filter { it.isVisible }
    private fun DartzeeRuleCarousel.getDisplayedRules() = getDisplayedTiles().map { it.dto }
    private fun makeCarousel(listener: IDartzeeCarouselListener = mockk(relaxed = true), dtos: List<DartzeeRuleDto> = this.dtos) = DartzeeRuleCarousel(
        dtos
    ).also { it.listener = listener }
    private fun DartzeeRuleCarousel.getPendingRules() = getAvailableRuleTiles().map { it.dto }
}