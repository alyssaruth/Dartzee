package burlton.dartzee.test.achievements.golf

import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.achievements.golf.AchievementGolfPointsRisked
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import burlton.dartzee.test.helper.insertRound

class TestAchievementGolfPointsRisked: AbstractAchievementTest<AchievementGolfPointsRisked>()
{
    override val gameType = GAME_TYPE_GOLF

    override fun factoryAchievement() = AchievementGolfPointsRisked()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        val rnd = insertRound(participantId = pt.rowId, roundNumber = 1)

        insertDart(roundId = rnd.rowId, ordinal = 1, score = rnd.roundNumber, multiplier = 1, segmentType = SEGMENT_TYPE_OUTER_SINGLE)
        insertDart(roundId = rnd.rowId, ordinal = 2, score = rnd.roundNumber, multiplier = 2, segmentType = SEGMENT_TYPE_DOUBLE)
    }
}