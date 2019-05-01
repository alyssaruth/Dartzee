package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementX01CheckoutCompleteness
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.helper.*
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01CheckoutCompleteness: TestAbstractAchievementRowPerGame<AchievementX01CheckoutCompleteness>()
{
    override fun factoryAchievement() = AchievementX01CheckoutCompleteness()

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val g = insertGame(gameType = GAME_TYPE_GOLF)
        insertCheckout(insertPlayer(), g, 1)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val g = insertRelevantGame()
        insertCheckout(p, g, 1)
    }

    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_X01)
    private fun insertCheckout(p: PlayerEntity, g: GameEntity, score: Int = 1)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, startingScore = score*2, score = score, multiplier = 2)
    }
}