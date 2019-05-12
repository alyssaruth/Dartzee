package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.x01.AchievementX01BestFinish
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import burlton.desktopcore.code.util.getSqlDateNow

class TestAchievementX01BestFinish: AbstractAchievementTest<AchievementX01BestFinish>()
{
    override val gameType = GAME_TYPE_X01

    override fun factoryAchievement() = AchievementX01BestFinish()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, dtFinished = getSqlDateNow())


        insertDart(playerId = p.rowId, participantId = pt.rowId, ordinal = 1, startingScore = 60, score = 20, multiplier = 1)
        insertDart(playerId = p.rowId, participantId = pt.rowId, ordinal = 2, startingScore = 40, score = 20, multiplier = 2)
    }

    //TODO - Finish me
}