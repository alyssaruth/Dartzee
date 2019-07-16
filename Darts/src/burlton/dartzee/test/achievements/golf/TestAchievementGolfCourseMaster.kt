package burlton.dartzee.test.achievements.golf

import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.achievements.golf.AchievementGolfCourseMaster
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.TestAbstractAchievementRowPerGame
import burlton.dartzee.test.helper.insertAchievement
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import java.sql.Timestamp

class TestAchievementGolfCourseMaster: TestAbstractAchievementRowPerGame<AchievementGolfCourseMaster>()
{
    override val gameType = GAME_TYPE_GOLF
    override fun factoryAchievement() = AchievementGolfCourseMaster()
    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SEGMENT_TYPE_DOUBLE)
    }

    override fun insertAchievementRow(dtLastUpdate: Timestamp): AchievementEntity
    {
        return insertAchievement(dtLastUpdate = dtLastUpdate, achievementDetail = "2")
    }
}