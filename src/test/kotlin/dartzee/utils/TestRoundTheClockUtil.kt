package dartzee.utils

import dartzee.`object`.Dart
import dartzee.db.CLOCK_TYPE_DOUBLES
import dartzee.helper.AbstractTest
import dartzee.helper.factoryClockHit
import dartzee.helper.randomGuid
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test

class TestRoundTheClockUtil: AbstractTest()
{
    @Test
    fun `getLongestStreak should return 0 for an empty list`()
    {
        getLongestStreak(listOf()).shouldBeEmpty()
    }

    @Test
    fun `getLongestStreak should take into account the game params`()
    {
        getLongestStreak(listOf(factoryClockHit(1, 1)), CLOCK_TYPE_DOUBLES) shouldHaveSize 0
        getLongestStreak(listOf(factoryClockHit(1, 2)), CLOCK_TYPE_DOUBLES) shouldHaveSize 1
    }

    @Test
    fun `getLongestStreak should find the longest streak if multiple`()
    {
        val darts = listOf(factoryClockHit(1),
                factoryClockHit(2),
                Dart(0, 0),
                factoryClockHit(3),
                factoryClockHit(4),
                factoryClockHit(5),
                Dart(0, 0),
                factoryClockHit(6),
                factoryClockHit(7))

        val streak = getLongestStreak(darts)
        streak shouldHaveSize 3
        streak.joinToString{ it.format() } shouldBe "3, 4, 5"
    }

    @Test
    fun `Should return the first instance if multiple longest streaks`()
    {
        val darts = listOf(factoryClockHit(1),
                factoryClockHit(2),
                Dart(0, 0),
                factoryClockHit(3),
                factoryClockHit(4),
                Dart(20, 1),
                factoryClockHit(6),
                factoryClockHit(7))

        val streak = getLongestStreak(darts)
        streak shouldHaveSize 2
        streak.joinToString{ it.format() } shouldBe "1, 2"
    }

    @Test
    fun `Should partition by participant`()
    {
        val firstDarts = listOf(factoryClockHit(1), factoryClockHit(2))
        val secondDarts = listOf(factoryClockHit(1), factoryClockHit(2), factoryClockHit(3))

        val ptOne = randomGuid()
        val ptTwo = randomGuid()
        firstDarts.forEach{ it.participantId = ptOne }
        secondDarts.forEach{ it.participantId = ptTwo }

        val streak = getLongestStreak(firstDarts + secondDarts)
        streak shouldHaveSize 3
    }
}