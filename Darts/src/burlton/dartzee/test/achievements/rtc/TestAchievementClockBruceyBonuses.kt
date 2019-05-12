package burlton.dartzee.test.achievements.rtc

import burlton.dartzee.code.achievements.rtc.AchievementClockBruceyBonuses
import burlton.dartzee.code.db.CLOCK_TYPE_STANDARD
import burlton.dartzee.code.db.GAME_TYPE_ROUND_THE_CLOCK
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertGame
import burlton.dartzee.test.helper.insertParticipant
import java.sql.Timestamp

class TestAchievementClockBruceyBonuses: AbstractAchievementTest<AchievementClockBruceyBonuses>()
{
    override val gameType = GAME_TYPE_ROUND_THE_CLOCK
    override fun factoryAchievement() = AchievementClockBruceyBonuses()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(playerId = pt.playerId, participantId = pt.rowId, ordinal = 4, startingScore = 4, score = 4, multiplier = 1)
    }

    override fun insertRelevantGame(dtLastUpdate: Timestamp): GameEntity
    {
        return insertGame(gameType = gameType, gameParams = CLOCK_TYPE_STANDARD, dtLastUpdate = dtLastUpdate)
    }

    //TODO - finish me
}