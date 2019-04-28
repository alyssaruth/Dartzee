package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementX01Btbf
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.helper.*
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01Btbf: AbstractAchievementTest<AchievementX01Btbf>()
{
    override fun factoryAchievement() = AchievementX01Btbf()

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val game = insertGame(gameType = GAME_TYPE_X01)
        insertSuccessfulParticipant(game, p)
    }

    @Test
    fun `Should insert a row for each double 1 achieved`()
    {
        val alice = insertPlayer("Alice")

        val game = insertGame(gameType = GAME_TYPE_X01)

        insertSuccessfulParticipant(game, alice)
        insertSuccessfulParticipant(game, alice)
        insertSuccessfulParticipant(game, alice)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 3
    }

    private fun insertSuccessfulParticipant(game: GameEntity, player: PlayerEntity)
    {
        val pt = insertParticipant(gameId = game.rowId, playerId = player.rowId, finalScore = 3)
        val rnd = insertRound(participantId = pt.rowId, roundNumber = 1)
        insertDart(roundId = rnd.rowId, startingScore = 2, score = 1, multiplier = 2)
    }
}