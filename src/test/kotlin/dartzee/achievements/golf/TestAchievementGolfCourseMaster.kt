package dartzee.achievements.golf

import dartzee.`object`.SegmentType
import dartzee.achievements.AchievementType
import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.retrieveAchievement
import dartzee.utils.Database
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
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
    fun `Should include games that were finished as part of a team`()
    {
        val pt = insertRelevantParticipant(team = true)
        insertDart(pt, score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 1
    }

    @Test
    fun `Should only insert the earliest example per hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, dtCreation = Timestamp(1000), score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE)
        insertDart(pt, dtCreation = Timestamp(500), score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion(emptyList())

        val a = retrieveAchievement()
        a.achievementDetail shouldBe "1"
        a.dtAchieved shouldBe Timestamp(500)
        a.achievementType shouldBe AchievementType.GOLF_COURSE_MASTER
    }

    @Test
    fun `Should ignore rows that arent doubles`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SegmentType.TREBLE)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore doubles that arent the right hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 2, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion(emptyList())
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should insert a row per distinct hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SegmentType.DOUBLE)
        insertDart(pt, score = 3, roundNumber = 3, segmentType = SegmentType.DOUBLE)

        factoryAchievement().populateForConversion(emptyList())

        getAchievementCount() shouldBe 2
    }
}