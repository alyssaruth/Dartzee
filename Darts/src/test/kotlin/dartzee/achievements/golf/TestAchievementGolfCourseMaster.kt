package dartzee.test.achievements.golf

import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.`object`.SEGMENT_TYPE_TREBLE
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import dartzee.achievements.golf.AchievementGolfCourseMaster
import dartzee.db.AchievementEntity
import dartzee.db.GAME_TYPE_GOLF
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.test.achievements.TestAbstractAchievementRowPerGame
import dartzee.test.helper.insertAchievement
import dartzee.test.helper.insertDart
import dartzee.test.helper.insertParticipant
import dartzee.test.helper.retrieveAchievement
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementGolfCourseMaster: TestAbstractAchievementRowPerGame<AchievementGolfCourseMaster>()
{
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

    @Test
    fun `Should only insert the earliest example per hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, dtCreation = Timestamp(1000), score = 1, roundNumber = 1, segmentType = SEGMENT_TYPE_DOUBLE)
        insertDart(pt, dtCreation = Timestamp(500), score = 1, roundNumber = 1, segmentType = SEGMENT_TYPE_DOUBLE)

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

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SEGMENT_TYPE_TREBLE)

        factoryAchievement().populateForConversion("")
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore doubles that arent the right hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 2, segmentType = SEGMENT_TYPE_DOUBLE)

        factoryAchievement().populateForConversion("")
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should insert a row per distinct hole`()
    {
        val pt = insertRelevantParticipant()

        insertDart(pt, score = 1, roundNumber = 1, segmentType = SEGMENT_TYPE_DOUBLE)
        insertDart(pt, score = 3, roundNumber = 3, segmentType = SEGMENT_TYPE_DOUBLE)

        factoryAchievement().populateForConversion("")

        getAchievementCount() shouldBe 2
    }
}