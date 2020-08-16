package dartzee.achievements

import dartzee.core.util.getSqlDateNow
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.utils.DatabaseUtil
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.Test
import java.sql.Timestamp
import javax.imageio.ImageIO

abstract class AbstractAchievementTest<E: AbstractAchievement>: AbstractTest()
{
    abstract fun factoryAchievement(): E
    abstract fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)

    protected fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val g = insertRelevantGame()
        setUpAchievementRowForPlayerAndGame(p, g)
    }

    protected fun getAchievementCount(): Int
    {
        val ref = factoryAchievement().achievementRef
        return DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM Achievement WHERE AchievementRef = $ref")
    }

    open fun insertRelevantGame(dtLastUpdate: Timestamp = getSqlDateNow()): GameEntity
    {
        return insertGame(gameType = factoryAchievement().gameType!!, dtLastUpdate = dtLastUpdate)
    }

    fun insertRelevantParticipant(player: PlayerEntity = insertPlayer()): ParticipantEntity
    {
        val g = insertRelevantGame()

        return insertParticipant(playerId = player.rowId, gameId = g.rowId)
    }

    @Test
    fun `Should not leave any temp tables lying around`()
    {
        val alice = insertPlayer(name = "Alice")
        setUpAchievementRowForPlayer(alice)
        factoryAchievement().populateForConversion("")

        dropUnexpectedTables().shouldBeEmpty()
    }

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val otherType = GameType.values().find { it != factoryAchievement().gameType }!!

        val p = insertPlayer()
        val g = insertGame(gameType = otherType)
        setUpAchievementRowForPlayerAndGame(p, g)

        factoryAchievement().populateForConversion("")
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should only generate data for specified players`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        setUpAchievementRowForPlayer(alice)
        setUpAchievementRowForPlayer(bob)

        factoryAchievement().populateForConversion("'${alice.rowId}'")

        getAchievementCount() shouldBe 1

        val achievement = AchievementEntity().retrieveEntities("")[0]
        achievement.playerId shouldBe alice.rowId
    }

    @Test
    fun `Should generate data for all players by default`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        setUpAchievementRowForPlayer(alice)
        setUpAchievementRowForPlayer(bob)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 2

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