package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.getCountFromTable
import burlton.dartzee.test.helper.insertPlayer
import burlton.dartzee.test.helper.wipeTable
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import javax.imageio.ImageIO

abstract class AbstractAchievementTest<E: AbstractAchievement>: AbstractDartsTest()
{
    abstract fun factoryAchievement(): E
    abstract fun setUpAchievementRowForPlayer(p: PlayerEntity)

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        wipeTable("Achievement")
        wipeTable("Game")
        wipeTable("Player")
        wipeTable("Participant")
        wipeTable("Round")
        wipeTable("Dart")
    }

    @Test
    fun `Should only generate data for specified players`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        setUpAchievementRowForPlayer(alice)
        setUpAchievementRowForPlayer(bob)

        factoryAchievement().populateForConversion("'${alice.rowId}'")

        getCountFromTable("Achievement") shouldBe 1

        val achievement = AchievementEntity().retrieveEntities("")[0]
        achievement.playerId shouldBe alice.rowId
    }

    @Test
    fun `Should only generate data for all players by default`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        setUpAchievementRowForPlayer(alice)
        setUpAchievementRowForPlayer(bob)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 2

        val players = AchievementEntity().retrieveEntities("").map{ it.playerId }
        players.shouldContainExactlyInAnyOrder(alice.rowId, bob.rowId)
    }

    @Test
    fun `Icon URL should be valid`()
    {
        val url = factoryAchievement().getIconURL()

        val bufferedImage = ImageIO.read(url)
        bufferedImage shouldNotBe null
    }

    @Test
    fun `Unbounded achievements should have MaxValue = PinkThreshold`()
    {
        val achievement = factoryAchievement()

        if (achievement.isUnbounded())
        {
            achievement.maxValue shouldBe achievement.pinkThreshold
        }
    }

    @Test
    fun `Thresholds should be strictly increasing or decreasing`()
    {
        val achievement = factoryAchievement()

        if (!achievement.isDecreasing())
        {
            achievement.redThreshold shouldBeLessThan achievement.orangeThreshold
            achievement.orangeThreshold shouldBeLessThan achievement.yellowThreshold
            achievement.yellowThreshold shouldBeLessThan achievement.greenThreshold
            achievement.greenThreshold shouldBeLessThan achievement.blueThreshold
            achievement.blueThreshold shouldBeLessThan achievement.pinkThreshold
            achievement.pinkThreshold shouldBeLessThanOrEqual achievement.maxValue
        }
        else
        {
            achievement.redThreshold shouldBeGreaterThan achievement.orangeThreshold
            achievement.orangeThreshold shouldBeGreaterThan achievement.yellowThreshold
            achievement.yellowThreshold shouldBeGreaterThan achievement.greenThreshold
            achievement.greenThreshold shouldBeGreaterThan achievement.blueThreshold
            achievement.blueThreshold shouldBeGreaterThan achievement.pinkThreshold
            achievement.pinkThreshold shouldBeGreaterThanOrEqual achievement.maxValue
        }
    }
}