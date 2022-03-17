package dartzee.achievements.dartzee

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDartzeeRoundResult
import dartzee.helper.insertParticipant
import dartzee.utils.Database

class TestAchievementDartzeeHalved: AbstractAchievementTest<AchievementDartzeeHalved>()
{
    override fun factoryAchievement() = AchievementDartzeeHalved()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, database = database)
        insertDartzeeRoundResult(pt, success = false, score = -100, database = database)
    }

}