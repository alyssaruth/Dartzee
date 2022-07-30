package dartzee.achievements

import dartzee.core.util.getSqlDateNow
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.matchers.numerics.shouldBeLessThanOrEqual
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import javax.imageio.ImageIO

abstract class AbstractAchievementTest<E: AbstractAchievement>: AbstractTest()
{
    protected val testRules = listOf(twoBlackOneWhite, scoreEighteens, innerOuterInner, totalIsFifty, allTwenties)

    @BeforeEach
    fun beforeEach()
    {
        mainDatabase.dropUnexpectedTables()
    }

    abstract fun factoryAchievement(): E
    abstract fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database = mainDatabase)

    protected fun setUpAchievementRowForPlayer(p: PlayerEntity, database: Database = mainDatabase)
    {
        val g = insertRelevantGame(database = database)
        setUpAchievementRowForPlayerAndGame(p, g, database)
    }

    protected fun getAchievementCount(database: Database = mainDatabase): Int
    {
        val type = factoryAchievement().achievementType
        return AchievementEntity(database).countWhere("AchievementType = '$type'")
    }

    open fun insertRelevantGame(dtLastUpdate: Timestamp = getSqlDateNow(), database: Database = mainDatabase): GameEntity
    {
        return insertGame(gameType = factoryAchievement().gameType!!, dtLastUpdate = dtLastUpdate, database = database)
    }

    fun insertRelevantParticipant(player: PlayerEntity = insertPlayer(), finalScore: Int = -1, team: Boolean = false): ParticipantEntity
    {
        val g = insertRelevantGame()
        val teamEntity = if (team) insertTeam(gameId = g.rowId) else null

        return insertParticipant(playerId = player.rowId, gameId = g.rowId, finalScore = finalScore, teamId = teamEntity?.rowId ?: "")
    }

    @Test
    fun `Should ignore games of the wrong type`()
    {
        if (!factoryAchievement().usesTransactionalTablesForConversion) return

        val otherType = GameType.values().find { it != factoryAchievement().gameType }!!

        val p = insertPlayer()
        val g = insertGame(gameType = otherType)
        setUpAchievementRowForPlayerAndGame(p, g)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should only generate data for specified players`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        setUpAchievementRowForPlayer(alice)
        setUpAchievementRowForPlayer(bob)

        factoryAchievement().populateForConversion(listOf(alice.rowId))

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

        factoryAchievement().populateForConversion(emptyList())

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
    fun `should run conversion on the right database`()
    {
        try
        {
            usingInMemoryDatabase(withSchema = true) { otherDatabase ->
                val alice = insertPlayer(name = "Alice", database = otherDatabase)
                setUpAchievementRowForPlayer(alice, otherDatabase)

                mainDatabase.shutDown() shouldBe true

                factoryAchievement().populateForConversion(emptyList(), otherDatabase)
                getAchievementCount(otherDatabase) shouldBe 1

                //If it's been connected to during the test, then another shut down would succeed
                mainDatabase.shutDown() shouldBe false
            }
        }
        finally
        {
            mainDatabase.initialiseConnectionPool(1)
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