package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01BestThreeDarts
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant

class TestAchievementX01BestThreeDarts: AbstractAchievementTest<AchievementX01BestThreeDarts>()
{
    override val gameType = GAME_TYPE_X01

    override fun factoryAchievement() = AchievementX01BestThreeDarts()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(playerId = p.rowId, participantId = pt.rowId, ordinal = 1, startingScore = 501, score = 20, multiplier = 3)
        insertDart(playerId = p.rowId, participantId = pt.rowId, ordinal = 2, startingScore = 441, score = 20, multiplier = 3)
        insertDart(playerId = p.rowId, participantId = pt.rowId, ordinal = 3, startingScore = 381, score = 20, multiplier = 3)
    }

    //TODO - finish me
}