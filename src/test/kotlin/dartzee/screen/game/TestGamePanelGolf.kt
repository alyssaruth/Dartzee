package dartzee.screen.game

import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.drtDoubleOne
import dartzee.drtDoubleThree
import dartzee.drtDoubleTwo
import dartzee.drtInnerThree
import dartzee.drtOuterOne
import dartzee.helper.AbstractTest
import dartzee.helper.AchievementSummary
import dartzee.helper.insertAchievement
import dartzee.helper.preparePlayers
import dartzee.helper.randomGuid
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestGamePanelGolf: AbstractTest()
{
    /**
     * Updating Gambler achievement
     */
    @Test
    fun `It should not update gambler achievement for missed darts`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(
                Dart(1, 0, segmentType = SegmentType.MISS),
                Dart(20, 3, segmentType = SegmentType.TREBLE),
                Dart(1, 1, segmentType = SegmentType.OUTER_SINGLE))

        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId) shouldBe null
    }

    @Test
    fun `It should sum up all the points gambled in that round`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(
                Dart(1, 3, segmentType = SegmentType.TREBLE),
                Dart(1, 3, segmentType = SegmentType.OUTER_SINGLE),
                Dart(1, 1, segmentType = SegmentType.TREBLE))

        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val a = AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId)!!
        a.achievementCounter shouldBe 4
    }

    @Test
    fun `It should compute correctly when just two darts thrown`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(
            Dart(1, 1, segmentType = SegmentType.OUTER_SINGLE),
            Dart(1, 1, segmentType = SegmentType.INNER_SINGLE))

        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val a = AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId)!!
        a.achievementCounter shouldBe 1
    }

    @Test
    fun `It should do nothing for a single dart hit`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(Dart(1, 1, segmentType = SegmentType.TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId) shouldBe null
    }

    /**
     * Updating Course Master achievement
     */
    @Test
    fun `Should not count darts that aren't a hole in one`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(Dart(1, 3, segmentType = SegmentType.TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should not count darts for the wrong hole`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(Dart(2, 2, segmentType = SegmentType.DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should only count the last dart thrown`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(Dart(1, 2, segmentType = SegmentType.DOUBLE), Dart(1, 3, segmentType = SegmentType.TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should insert a row for a new hole in one`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)
        insertAchievement(playerId = playerId, type = AchievementType.GOLF_COURSE_MASTER, achievementDetail = "2")

        val darts = listOf(Dart(1, 2, segmentType = SegmentType.DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val rows = AchievementEntity().retrieveEntities("PlayerId = '$playerId' AND AchievementType = '${AchievementType.GOLF_COURSE_MASTER}'")
        rows.size shouldBe 2
        rows.map { it.achievementDetail }.shouldContainExactlyInAnyOrder("1", "2")
    }
    @Test
    fun `Should not insert a row for a hole in one already attained`()
    {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)
        val originalRow = insertAchievement(playerId = playerId, type = AchievementType.GOLF_COURSE_MASTER, achievementDetail = "1")

        val darts = listOf(Dart(1, 2, segmentType = SegmentType.DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val newRow = AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId)!!
        newRow.rowId shouldBe originalRow.rowId
    }

    /**
     * Team achievements
     */
    @Test
    fun `Should unlock the correct achievements for team play`()
    {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val panel = makeGolfGamePanel(team)
        val gameId = panel.gameEntity.rowId

        val roundOne = listOf(drtOuterOne(), drtOuterOne(), drtDoubleOne()) // P1: Risked 2, CM: 1
        panel.addCompletedRound(roundOne)

        val roundTwo = listOf(drtDoubleTwo()) // P2: Risked 0, CM: 2
        panel.addCompletedRound(roundTwo)

        val roundThree = listOf(drtInnerThree(), drtDoubleThree()) // P1: Risked 4, CM: 1, 3
        panel.addCompletedRound(roundThree)

        retrieveAchievementsForPlayer(p1.rowId).shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, gameId, "1"),
            AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, gameId, "3"),
            AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, gameId, "1"),
            AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, gameId, "3")
        )

        retrieveAchievementsForPlayer(p2.rowId).shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, gameId, "2"),
        )
    }
}