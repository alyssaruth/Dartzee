package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01SuchBadLuck
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant

class TestAchievementX01SuchBadLuck: AbstractAchievementTest<AchievementX01SuchBadLuck>()
{
    override val gameType = GAME_TYPE_X01

    override fun factoryAchievement() = AchievementX01SuchBadLuck()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, ordinal = 1, startingScore = 2, score = 20, multiplier = 2)
    }

    //TODO - finish me
}