package dartzee.screen.game

import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.TestAchievementEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.screen.game.golf.GamePanelGolf
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
        val panel = TestGamePanel(playerId)

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
        val panel = TestGamePanel(playerId)

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
        val panel = TestGamePanel(playerId)

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
        val panel = TestGamePanel(playerId)

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
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(1, 3, segmentType = SegmentType.TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should not count darts for the wrong hole`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(2, 2, segmentType = SegmentType.DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should only count the last dart thrown`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(1, 2, segmentType = SegmentType.DOUBLE), Dart(1, 3, segmentType = SegmentType.TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should insert a row for a new hole in one`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)
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
        val panel = TestGamePanel(playerId)
        val originalRow = insertAchievement(playerId = playerId, type = AchievementType.GOLF_COURSE_MASTER, achievementDetail = "1")

        val darts = listOf(Dart(1, 2, segmentType = SegmentType.DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val newRow = AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId)!!
        newRow.rowId shouldBe originalRow.rowId
    }

    private class TestGamePanel(currentPlayerId: String = randomGuid())
        : GamePanelGolf(TestAchievementEntity.FakeDartsScreen(), GameEntity.factoryAndSave(GameType.GOLF, "18"), 1)
    {
        init
        {
            val player = insertPlayer(currentPlayerId)

            val scorer = assignScorer(player)

            currentPlayerNumber = 0

            addState(0, makeGolfPlayerState(player), scorer)

            currentRoundNumber = 1
        }

        fun setDartsThrown(dartsThrown: List<Dart>)
        {
            getCurrentPlayerState().resetRound()
            dartsThrown.forEach { getCurrentPlayerState().dartThrown(it) }
        }
    }
}