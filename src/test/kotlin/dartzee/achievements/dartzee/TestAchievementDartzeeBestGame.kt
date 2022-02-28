package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.*
import dartzee.utils.Database
import dartzee.utils.insertDartzeeRules
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementDartzeeBestGame: AbstractAchievementTest<AchievementDartzeeBestGame>()
{
    private val testRules = listOf(twoBlackOneWhite, scoreEighteens, innerOuterInner, totalIsFifty, allTwenties)

    override fun factoryAchievement() = AchievementDartzeeBestGame()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 125, database = database)

        insertDartzeeRules(g.rowId, testRules, database)
    }

    @Test
    fun `should ignore games with fewer than 5 rules`()
    {
        val pt = insertRelevantParticipant(finalScore = 120)

        val shortList = testRules.subList(0, DARTZEE_BEST_GAME_MIN_ROUNDS - 1)
        insertDartzeeRules(pt.gameId, shortList)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `should ignore unfinished participants`()
    {
        val pt = insertRelevantParticipant(finalScore = -1)
        insertDartzeeRules(pt.gameId, testRules)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should compute score as average per round, rounded down`()
    {
        val player = insertPlayer()
        setUpAchievementRowForPlayer(player)

        factoryAchievement().populateForConversion(emptyList())
        retrieveAchievement().achievementCounter shouldBe 20
    }
}