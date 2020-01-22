package dartzee.achievements.x01

import dartzee.achievements.x01.AchievementX01HighestBust
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.achievements.AbstractAchievementTest
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant

class TestAchievementX01HighestBust: AbstractAchievementTest<AchievementX01HighestBust>()
{
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