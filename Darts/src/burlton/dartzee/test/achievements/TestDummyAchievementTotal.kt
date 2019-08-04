package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.*
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertAchievement
import burlton.dartzee.test.helper.wipeTable
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test

class TestDummyAchievementTotal: AbstractDartsTest()
{
    @Test
    fun `Should not be in the list of all achievements`()
    {
        getAllAchievements().forEach{
            it.javaClass shouldNotBe DummyAchievementTotal().javaClass
        }
    }

    @Test
    fun `Should retrieve all rows regardless of achievement reference`()
    {
        wipeTable("Achievement")

        val a1 = insertAchievement(achievementRef = ACHIEVEMENT_REF_X01_BTBF)
        val a2 = insertAchievement(achievementRef = ACHIEVEMENT_REF_X01_SUCH_BAD_LUCK)

        val rows = DummyAchievementTotal().retrieveAllRows().map{ it.rowId }

        rows.shouldContainExactlyInAnyOrder(a1.rowId, a2.rowId)
    }

    @Test
    fun `Should have sensible thresholds`()
    {
        val achievement = DummyAchievementTotal()

        achievement.redThreshold shouldBeLessThan achievement.orangeThreshold
        achievement.orangeThreshold shouldBeLessThan achievement.yellowThreshold
        achievement.yellowThreshold shouldBeLessThan achievement.greenThreshold
        achievement.greenThreshold shouldBeLessThan achievement.blueThreshold
        achievement.blueThreshold shouldBeLessThan achievement.pinkThreshold
        achievement.pinkThreshold shouldBeLessThan achievement.maxValue

        achievement.maxValue shouldBe getAchievementMaximum()
    }

}
