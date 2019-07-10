package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01NoMercy
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.TestAbstractAchievementRowPerGame
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant

class TestAchievementX01NoMercy: TestAbstractAchievementRowPerGame<AchievementX01NoMercy>()
{
    override val gameType = GAME_TYPE_X01

    override fun factoryAchievement() = AchievementX01NoMercy()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 21)

        insertDart(pt, roundNumber = 7, startingScore = 7, score = 3, multiplier = 1, ordinal = 1)
        insertDart(pt, roundNumber = 7, startingScore = 4, score = 2, multiplier = 2, ordinal = 2)
    }
}