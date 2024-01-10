package dartzee.achievements.golf

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.achievements.AchievementType
import dartzee.core.util.getSqlDateNow
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.helper.AchievementSummary
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.`object`.SegmentType
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import org.junit.jupiter.api.Test

class TestAchievementGolfPointsRisked :
    AbstractMultiRowAchievementTest<AchievementGolfPointsRisked>() {
    override fun factoryAchievement() = AchievementGolfPointsRisked()

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database
    ) = insertRiskedDart(p.rowId, g.rowId, database = database)

    @Test
    fun `Should include games that were finished as part of a team`() {
        val pt = insertRelevantParticipant(team = true)
        insertRiskedDart(pt, SegmentType.INNER_SINGLE, 1)

        runConversion()
        getAchievementCount() shouldBe 1
    }

    @Test
    fun `Should ignore rounds where no points were risked`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        insertDart(
            pt,
            roundNumber = 1,
            ordinal = 1,
            score = 1,
            multiplier = 1,
            segmentType = SegmentType.DOUBLE
        )

        runConversion()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, p.rowId) shouldBe
            null
    }

    @Test
    fun `Should add a row per round, including round number`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertRiskedDart(p.rowId, g.rowId, SegmentType.INNER_SINGLE, 1)
        insertRiskedDart(p.rowId, g.rowId, SegmentType.OUTER_SINGLE, 2)

        runConversion()

        val achievementRows = retrieveAchievementsForPlayer(p.rowId)
        achievementRows.shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, g.rowId, "1"),
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 1, g.rowId, "2")
        )
    }

    @Test
    fun `Should aggregate across a round, taking the latest DtCreation and sum of points risked`() {
        val p = insertPlayer()
        val pt = insertRelevantParticipant(p)

        insertDart(
            pt,
            roundNumber = 1,
            ordinal = 1,
            score = 1,
            multiplier = 3,
            segmentType = SegmentType.TREBLE,
            dtCreation = Timestamp(500)
        )
        insertDart(
            pt,
            roundNumber = 1,
            ordinal = 2,
            score = 1,
            multiplier = 1,
            segmentType = SegmentType.INNER_SINGLE,
            dtCreation = Timestamp(1000)
        )
        insertDart(
            pt,
            roundNumber = 1,
            ordinal = 3,
            score = 1,
            multiplier = 2,
            segmentType = SegmentType.DOUBLE,
            dtCreation = Timestamp(1500)
        )

        runConversion()

        val achievementRow = retrieveAchievement()
        achievementRow.playerId shouldBe p.rowId
        achievementRow.gameIdEarned shouldBe pt.gameId
        achievementRow.achievementCounter shouldBe 5
        achievementRow.achievementDetail shouldBe "1"
        achievementRow.dtAchieved shouldBe Timestamp(1000)
    }

    @Test
    fun `Should map segment types correctly`() {
        verifySegmentTypeScoredCorrectly(SegmentType.OUTER_SINGLE, 1)
        verifySegmentTypeScoredCorrectly(SegmentType.INNER_SINGLE, 2)
        verifySegmentTypeScoredCorrectly(SegmentType.TREBLE, 3)
        verifySegmentTypeScoredCorrectly(SegmentType.DOUBLE, 4)
        verifySegmentTypeScoredCorrectly(SegmentType.MISS, 0)
    }

    private fun verifySegmentTypeScoredCorrectly(segmentType: SegmentType, expectedScore: Int) {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertRiskedDart(p.rowId, g.rowId, segmentType)

        runConversion()

        val a = AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, p.rowId)!!
        a.achievementCounter shouldBe expectedScore
    }

    private fun insertRiskedDart(
        playerId: String,
        gameId: String,
        segmentType: SegmentType = SegmentType.OUTER_SINGLE,
        roundNumber: Int = 1,
        dtCreation: Timestamp = getSqlDateNow(),
        database: Database = mainDatabase
    ) {
        val pt = insertParticipant(playerId = playerId, gameId = gameId, database = database)
        insertRiskedDart(pt, segmentType, roundNumber, dtCreation, database)
    }

    private fun insertRiskedDart(
        pt: ParticipantEntity,
        segmentType: SegmentType = SegmentType.OUTER_SINGLE,
        roundNumber: Int = 1,
        dtCreation: Timestamp = getSqlDateNow(),
        database: Database = mainDatabase
    ) {
        insertDart(
            pt,
            roundNumber = roundNumber,
            ordinal = 1,
            score = roundNumber,
            multiplier = 1,
            segmentType = segmentType,
            dtCreation = dtCreation,
            database = database
        )
        insertDart(
            pt,
            roundNumber = roundNumber,
            ordinal = 2,
            score = roundNumber,
            multiplier = 2,
            segmentType = SegmentType.DOUBLE,
            dtCreation = dtCreation,
            database = database
        )
    }
}
