package dartzee.achievements.golf

import dartzee.`object`.SegmentType
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import dartzee.achievements.AbstractAchievementTest
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
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
    fun `Should ignore rounds where no points were risked`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        insertDart(pt, roundNumber = 1, ordinal = 1, score = 1, multiplier = 1, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion(emptyList())

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, p.rowId) shouldBe null
    }

    @Test
    fun `Should combine multiple rounds correctly`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertRiskedDart(p, g, SegmentType.INNER_SINGLE, 1)
        insertRiskedDart(p, g, SegmentType.OUTER_SINGLE, 2)

        factoryAchievement().populateForConversion(emptyList())

        val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, p.rowId)!!
        a.achievementCounter shouldBe 3
    }


    @Test
    fun `Should map segment types correctly`()
    {
        verifySegmentTypeScoredCorrectly(SegmentType.OUTER_SINGLE, 1)
        verifySegmentTypeScoredCorrectly(SegmentType.INNER_SINGLE, 2)
        verifySegmentTypeScoredCorrectly(SegmentType.TREBLE, 3)
        verifySegmentTypeScoredCorrectly(SegmentType.DOUBLE, 4)
        verifySegmentTypeScoredCorrectly(SegmentType.MISS, 0)
    }
    private fun verifySegmentTypeScoredCorrectly(segmentType: SegmentType, expectedScore: Int)
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertRiskedDart(p, g, segmentType)

        factoryAchievement().populateForConversion(emptyList())

        val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, p.rowId)!!
        a.achievementCounter shouldBe expectedScore
    }

    fun insertRiskedDart(p: PlayerEntity, g: GameEntity, segmentType: SegmentType = SegmentType.OUTER_SINGLE, roundNumber: Int = 1)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDart(pt, roundNumber = roundNumber, ordinal = 1, score = roundNumber, multiplier = 1, segmentType = segmentType)
        insertDart(pt, roundNumber = roundNumber, ordinal = 2, score = roundNumber, multiplier = 2, segmentType = SegmentType.DOUBLE)

    }
}