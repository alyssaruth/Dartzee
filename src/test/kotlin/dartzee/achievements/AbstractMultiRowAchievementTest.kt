package dartzee.achievements

import dartzee.core.util.getSqlDateNow
import dartzee.helper.insertAchievement
import dartzee.helper.insertPlayer
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.sql.Timestamp

abstract class AbstractMultiRowAchievementTest<E: AbstractMultiRowAchievement>: AbstractAchievementTest<E>()
{
    private fun getDtAchievedColumnIndex() = factoryAchievement().getBreakdownColumns().indexOf("Date Achieved")
    fun getGameIdEarnedColumnIndex() = factoryAchievement().getBreakdownColumns().indexOf("Game")

    private fun insertAchievementRow(dtLastUpdate: Timestamp = getSqlDateNow(),
                                  achievementCounter: Int = -1)
            = insertAchievement(dtLastUpdate = dtLastUpdate, achievementCounter = achievementCounter, achievementDetail = "10")

    @Test
    fun `Breakdown column count should match row length`()
    {
        val a = factoryAchievement()

        a.getBreakdownColumns().size shouldBe a.getBreakdownRow(insertAchievementRow()).size
    }

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
    fun `Should set the attainedValue correctly, taking into account all rows`()
    {
        val a = factoryAchievement()
        if (a.useCounter())
        {
            a.initialiseFromDb(listOf(
                insertAchievementRow(achievementCounter = 1),
                insertAchievementRow(achievementCounter = 2),
                insertAchievementRow(achievementCounter = 4)), null)
            a.attainedValue shouldBe 7

            a.initialiseFromDb(listOf(insertAchievementRow(achievementCounter = 8)), null)
            a.attainedValue shouldBe 8
        }
        else
        {
            a.initialiseFromDb(listOf(insertAchievementRow(), insertAchievementRow(), insertAchievementRow()), null)
            a.attainedValue shouldBe 3

            a.initialiseFromDb(listOf(insertAchievementRow()), null)
            a.attainedValue shouldBe 1
        }
    }

    @Test
    fun `Should sort the rows by dtLastUpdate`()
    {
        val achievementOne = insertAchievementRow(dtLastUpdate = Timestamp(500))
        val achievementTwo = insertAchievementRow(dtLastUpdate = Timestamp(1000))
        val achievementThree = insertAchievementRow(dtLastUpdate = Timestamp(1500))
        val achievementFour = insertAchievementRow(dtLastUpdate = Timestamp(2000))

        val a = factoryAchievement()
        a.initialiseFromDb(listOf(achievementTwo, achievementFour, achievementThree, achievementOne), null)

        a.dtLatestUpdate shouldBe Timestamp(2000)
        a.tmBreakdown shouldNotBe null

        a.tmBreakdown!!.getValueAt(0, getDtAchievedColumnIndex()) shouldBe Timestamp(500)
        a.tmBreakdown!!.getValueAt(1, getDtAchievedColumnIndex()) shouldBe Timestamp(1000)
        a.tmBreakdown!!.getValueAt(2, getDtAchievedColumnIndex()) shouldBe Timestamp(1500)
        a.tmBreakdown!!.getValueAt(3, getDtAchievedColumnIndex()) shouldBe Timestamp(2000)
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

        val dbRow = insertAchievementRow()
        dbRow.localGameIdEarned = 20

        a.initialiseFromDb(listOf(dbRow), null)

        a.tmBreakdown!!.getValueAt(0, getGameIdEarnedColumnIndex()) shouldBe 20
    }
}