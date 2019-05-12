package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01HighestBust
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant

class TestAchievementX01HighestBust: AbstractAchievementTest<AchievementX01HighestBust>()
{
    override val gameType = GAME_TYPE_X01

    override fun factoryAchievement() = AchievementX01HighestBust()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 1, startingScore = 181, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 2, startingScore = 121, score = 20, multiplier = 3)
        insertDart(pt, ordinal = 3, startingScore = 61, score = 20, multiplier = 3)
    }

    //TODO - finish me
}