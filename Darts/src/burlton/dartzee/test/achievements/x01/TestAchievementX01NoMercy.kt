package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01NoMercy
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.TestAbstractAchievementRowPerGame
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import burlton.dartzee.test.helper.insertPlayer
import burlton.dartzee.test.helper.retrieveAchievement
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01NoMercy: TestAbstractAchievementRowPerGame<AchievementX01NoMercy>()
{
    override fun factoryAchievement() = AchievementX01NoMercy()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 21)

        insertDart(pt, roundNumber = 7, startingScore = 7, ordinal = 1)
    }

    @Test
    fun `Should not include data for an unfinished player`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = -1)

        insertDart(pt, roundNumber = 1, startingScore = 7, ordinal = 1)

        factoryAchievement().populateForConversion("")
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should only count if the first dart has the right startingScore`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 21)
        insertDart(pt, roundNumber = 7, ordinal = 1, startingScore = 12, score = 5, multiplier = 1)
        insertDart(pt, roundNumber = 7, ordinal = 2, startingScore = 7, score = 3, multiplier = 1)

        factoryAchievement().populateForConversion("")
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should insert a row for every successful finish, even with the same startingScore`()
    {
        val alice = insertPlayer(name = "Alice")

        setUpAchievementRowForPlayer(alice)
        setUpAchievementRowForPlayer(alice)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 2
    }

    @Test
    fun `Should count finishes of 3, 5, 7 and 9`()
    {
        val alice = insertPlayer(name = "Alice")

        setUpFinishForPlayer(alice, 3)
        setUpFinishForPlayer(alice, 5)
        setUpFinishForPlayer(alice, 7)
        setUpFinishForPlayer(alice, 9)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 4
    }

    @Test
    fun `Should count higher odd numbers, or even finishes`()
    {
        val alice = insertPlayer(name = "Alice")

        setUpFinishForPlayer(alice, 2)
        setUpFinishForPlayer(alice, 4)
        setUpFinishForPlayer(alice, 11)
        setUpFinishForPlayer(alice, 13)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should set the detail on the achievement row to be the finish that was attained`()
    {
        val p = insertPlayer()
        setUpFinishForPlayer(p, 5)

        factoryAchievement().populateForConversion("")

        retrieveAchievement().achievementCounter shouldBe -1
        retrieveAchievement().achievementDetail shouldBe "5"
    }

    private fun setUpFinishForPlayer(p: PlayerEntity, finish: Int)
    {
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 21)

        insertDart(pt, roundNumber = 7, startingScore = finish, ordinal = 1)
    }
}