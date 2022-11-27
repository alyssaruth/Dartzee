package dartzee.achievements

import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class TestDummyAchievementTotal: AbstractTest()
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
        val a1 = insertAchievement(type = AchievementType.X01_BTBF)
        val a2 = insertAchievement(type = AchievementType.X01_SUCH_BAD_LUCK)

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
