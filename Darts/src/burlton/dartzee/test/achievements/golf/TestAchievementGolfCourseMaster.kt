package burlton.dartzee.test.achievements.golf

import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import burlton.dartzee.code.achievements.golf.AchievementGolfCourseMaster
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_GOLF
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.TestAbstractAchievementRowPerGame
import burlton.dartzee.test.helper.insertAchievement
import burlton.dartzee.test.helper.insertDart
import burlton.dartzee.test.helper.insertParticipant
import burlton.dartzee.test.helper.retrieveAchievement
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementGolfCourseMaster: TestAbstractAchievementRowPerGame<AchievementGolfCourseMaster>()
{
    override val gameType = GAME_TYPE_GOLF
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