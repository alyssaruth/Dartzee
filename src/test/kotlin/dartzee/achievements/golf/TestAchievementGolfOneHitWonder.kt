package dartzee.achievements.golf

import dartzee.achievements.AbstractAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.drtDoubleFour
import dartzee.drtDoubleNineteen
import dartzee.drtDoubleOne
import dartzee.drtDoubleSeventeen
import dartzee.drtInnerTwo
import dartzee.drtOuterOne
import dartzee.drtOuterThree
import dartzee.drtOuterTwo
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.makeGolfRound
import dartzee.utils.Database

class TestAchievementGolfOneHitWonder : AbstractAchievementTest<AchievementGolfOneHitWonder>()
{
    override fun factoryAchievement() = AchievementGolfOneHitWonder()

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)
        val dartRounds = listOf(
            makeGolfRound(1, listOf(drtOuterOne(), drtDoubleOne())),
            makeGolfRound(2, listOf(drtOuterTwo(), drtInnerTwo())),
            makeGolfRound(3, listOf(drtOuterThree(), drtDoubleNineteen(), drtDoubleSeventeen())),
            makeGolfRound(4, listOf(drtDoubleFour()))
        )

        dartRounds.flatten().forEach {
            insertDart(pt, roundNumber = it.roundNumber, ordinal = it.ordinal, score = it.score, multiplier = it.multiplier, segmentType = it.segmentType, database = database)
        }
    }



}