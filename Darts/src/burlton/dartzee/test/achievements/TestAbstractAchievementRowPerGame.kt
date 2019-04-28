package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AbstractAchievementRowPerGame
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.test.helper.insertAchievement
import burlton.dartzee.test.helper.insertPlayer
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.sql.Timestamp

abstract class TestAbstractAchievementRowPerGame<E: AbstractAchievementRowPerGame>: AbstractAchievementTest<E>()
{
    @Test
    fun `Should handle 0 achievement rows`()
    {
        val a = factoryAchievement()

        a.initialiseFromDb(listOf(), null)

        a.tmBreakdown shouldBe null
        a.attainedValue shouldBe 0
        a.player shouldBe null
    }

    @Test
    fun `Should set the attainedValue to the number of rows`()
    {
        val a = factoryAchievement()

        a.initialiseFromDb(listOf(AchievementEntity(), AchievementEntity(), AchievementEntity()), null)
        a.attainedValue shouldBe 3

        a.initialiseFromDb(listOf(AchievementEntity()), null)
        a.attainedValue shouldBe 1
    }

    @Test
    fun `Should sort the rows by dtLastUpdate`()
    {
        val achievementOne = insertAchievement(dtLastUpdate = Timestamp(500))
        val achievementTwo = insertAchievement(dtLastUpdate = Timestamp(1000))
        val achievementThree = insertAchievement(dtLastUpdate = Timestamp(1500))
        val achievementFour = insertAchievement(dtLastUpdate = Timestamp(2000))

        val a = factoryAchievement()
        a.initialiseFromDb(listOf(achievementTwo, achievementFour, achievementThree, achievementOne), null)

        a.dtLatestUpdate shouldBe Timestamp(2000)
        a.tmBreakdown shouldNotBe null

        a.tmBreakdown!!.getValueAt(0, 1) shouldBe Timestamp(500)
        a.tmBreakdown!!.getValueAt(1, 1) shouldBe Timestamp(1000)
        a.tmBreakdown!!.getValueAt(2, 1) shouldBe Timestamp(1500)
        a.tmBreakdown!!.getValueAt(3, 1) shouldBe Timestamp(2000)
    }

    @Test
    fun `Should set the player`()
    {
        val player = insertPlayer()
        val a = factoryAchievement()

        a.initialiseFromDb(listOf(), player)
        a.player shouldBe player
    }

    @Test
    fun `Should display the local game ID in the breakdown`()
    {
        val a = factoryAchievement()

        val dbRow = insertAchievement()
        dbRow.localGameIdEarned = 20

        a.initialiseFromDb(listOf(dbRow), null)

        a.tmBreakdown!!.getValueAt(0, 0) shouldBe 20
    }
}