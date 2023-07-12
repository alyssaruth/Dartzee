package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.utils.Database

class TestAchievementX01StylishFinish : AbstractMultiRowAchievementTest<AchievementX01StylishFinish>()
{
    override fun factoryAchievement() = AchievementX01StylishFinish()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)
        insertDart(pt, startingScore = 101, roundNumber = 1, ordinal = 1, score = 19, multiplier = 3, database = database)
        insertDart(pt, startingScore = 44, roundNumber = 1, ordinal = 2, score = 4, multiplier = 1, database = database)
        insertDart(pt, startingScore = 40, roundNumber = 1, ordinal = 3, score = 20, multiplier = 2, database = database)
    }

}