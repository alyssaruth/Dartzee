package dartzee.achievements.golf

import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.`object`.SEGMENT_TYPE_INNER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_TREBLE
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import dartzee.achievements.golf.AchievementGolfPointsRisked
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.achievements.AbstractAchievementTest
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
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