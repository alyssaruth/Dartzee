package burlton.dartzee.test.achievements.golf

import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import burlton.dartzee.code.achievements.golf.AchievementGolfPointsRisked
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.AbstractAchievementTest
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import burlton.dartzee.test.helper.insertPlayer
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementGolfPointsRisked: AbstractAchievementTest<AchievementGolfPointsRisked>()
{
    override fun factoryAchievement() = AchievementGolfPointsRisked()
    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity) = insertRiskedDart(p, g)


    @Test
    fun `Should map segment types correctly`()
    {
        verifySegmentTypeScoredCorrectly(SEGMENT_TYPE_OUTER_SINGLE, 1)
        verifySegmentTypeScoredCorrectly(SEGMENT_TYPE_INNER_SINGLE, 2)
        verifySegmentTypeScoredCorrectly(SEGMENT_TYPE_TREBLE, 3)
        verifySegmentTypeScoredCorrectly(SEGMENT_TYPE_DOUBLE, 4)
    }
    private fun verifySegmentTypeScoredCorrectly(segmentType: Int, expectedScore: Int)
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertRiskedDart(p, g, segmentType)

        factoryAchievement().populateForConversion("")

        val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, p.rowId)!!
        a.achievementCounter shouldBe expectedScore
    }

    fun insertRiskedDart(p: PlayerEntity, g: GameEntity, segmentType: Int = SEGMENT_TYPE_OUTER_SINGLE)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, roundNumber = 1, ordinal = 1, score = 1, multiplier = 1, segmentType = segmentType)
        insertDart(pt, roundNumber = 1, ordinal = 2, score = 1, multiplier = 2, segmentType = SEGMENT_TYPE_DOUBLE)

    }
}