package dartzee.achievements.golf

import dartzee.`object`.SegmentType
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.retrieveAchievement
import dartzee.utils.Database
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementGolfCourseMaster: AbstractMultiRowAchievementTest<AchievementGolfCourseMaster>()
{
    override fun factoryAchievement() = AchievementGolfCourseMaster()
    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE, database = database)
    }

    @Test
    fun `Should only insert the earliest example per hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, dtCreation = Timestamp(1000), score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE)
        insertDart(pt, dtCreation = Timestamp(500), score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion("")

        val a = retrieveAchievement()
        a.achievementDetail shouldBe "1"
        a.dtLastUpdate shouldBe Timestamp(500)
        a.achievementRef shouldBe ACHIEVEMENT_REF_GOLF_COURSE_MASTER
    }

    @Test
    fun `Should ignore rows that arent doubles`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SegmentType.TREBLE)

        factoryAchievement().populateForConversion("")
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore doubles that arent the right hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 2, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion("")
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should insert a row per distinct hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE)
        insertDart(pt, score = 3, roundNumber = 3, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 2
    }
}