package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.AchievementX01HotelInspector
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertGame
import burlton.dartzee.test.helper.insertParticipant
import burlton.dartzee.test.helper.insertRound

class TestAchievementX01HotelInspector: TestAbstractAchievementRowPerGame<AchievementX01HotelInspector>()
{
    override fun factoryAchievement() = AchievementX01HotelInspector()

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        val rnd = insertRound(participantId = pt.rowId, roundNumber = 1)

        insertDart(roundId = rnd.rowId, score = 20, multiplier = 1, ordinal = 1, startingScore = 501)
        insertDart(roundId = rnd.rowId, score = 5, multiplier = 1, ordinal = 2, startingScore = 481)
        insertDart(roundId = rnd.rowId, score = 1, multiplier = 1, ordinal = 3, startingScore = 476)
    }

    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_X01)
}