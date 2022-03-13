package dartzee.achievements.dartzee

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertParticipant
import dartzee.utils.Database
import dartzee.utils.insertDartzeeRules

class TestAchievementDartzeeBingo: AbstractMultiRowAchievementTest<AchievementDartzeeBingo>()
{
    override fun factoryAchievement() = AchievementDartzeeBingo()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = 275, database = database)
        insertDartzeeRules(g.rowId, testRules, database)
    }
}