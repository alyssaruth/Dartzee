package burlton.dartzee.test.screen.game

import burlton.dartzee.code.`object`.*
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_GOLF_COURSE_MASTER
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_GOLF_POINTS_RISKED
import burlton.dartzee.code.db.*
import burlton.dartzee.code.screen.game.scorer.DartsScorerGolf
import burlton.dartzee.code.screen.game.GamePanelGolf
import burlton.dartzee.test.db.TestAchievementEntity
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertAchievement
import burlton.dartzee.test.helper.randomGuid
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test

class TestGamePanelGolf: AbstractDartsTest()
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
                Dart(1, 0, segmentType = SEGMENT_TYPE_MISS),
                Dart(20, 3, segmentType = SEGMENT_TYPE_TREBLE),
                Dart(1, 1, segmentType = SEGMENT_TYPE_OUTER_SINGLE))

        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, playerId) shouldBe null
    }
    @Test
    fun `It should sum up all the points gambled in that round`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(
                Dart(1, 3, segmentType = SEGMENT_TYPE_TREBLE),
                Dart(1, 3, segmentType = SEGMENT_TYPE_OUTER_SINGLE),
                Dart(1, 1, segmentType = SEGMENT_TYPE_TREBLE))

        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, playerId)!!
        a.achievementCounter shouldBe 4
    }
    @Test
    fun `It should do nothing for a single dart hit`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(1, 1, segmentType = SEGMENT_TYPE_TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_POINTS_RISKED, playerId) shouldBe null
    }

    /**
     * Updating Course Master achievement
     */
    @Test
    fun `Should not count darts that aren't a hole in one`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(1, 3, segmentType = SEGMENT_TYPE_TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should not count darts for the wrong hole`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(2, 2, segmentType = SEGMENT_TYPE_DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should only count the last dart thrown`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(1, 2, segmentType = SEGMENT_TYPE_DOUBLE), Dart(1, 3, segmentType = SEGMENT_TYPE_TREBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_COURSE_MASTER, playerId) shouldBe null
    }
    @Test
    fun `Should insert a row for a new hole in one`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)
        insertAchievement(playerId = playerId, achievementRef = ACHIEVEMENT_REF_GOLF_COURSE_MASTER, achievementDetail = "2")

        val darts = listOf(Dart(1, 2, segmentType = SEGMENT_TYPE_DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val rows = AchievementEntity().retrieveEntities("PlayerId = '$playerId' AND AchievementRef = $ACHIEVEMENT_REF_GOLF_COURSE_MASTER")
        rows.size shouldBe 2
        rows.map { it.achievementDetail }.shouldContainExactlyInAnyOrder("1", "2")
    }
    @Test
    fun `Should not insert a row for a hole in one already attained`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)
        val originalRow = insertAchievement(playerId = playerId, achievementRef = ACHIEVEMENT_REF_GOLF_COURSE_MASTER, achievementDetail = "1")

        val darts = listOf(Dart(1, 2, segmentType = SEGMENT_TYPE_DOUBLE))
        panel.setDartsThrown(darts)

        panel.unlockAchievements()

        val newRow = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_GOLF_COURSE_MASTER, playerId)!!
        newRow.rowId shouldBe originalRow.rowId
    }

    private class TestGamePanel(currentPlayerId: String = randomGuid())
        : GamePanelGolf(TestAchievementEntity.FakeDartsScreen(), GameEntity.factoryAndSave(GAME_TYPE_GOLF, "18"))
    {
        init
        {
            for (i in 0..3)
            {
                val scorer = DartsScorerGolf()
                scorer.init(PlayerEntity(), "18")
                hmPlayerNumberToDartsScorer[i] = scorer
            }

            activeScorer = hmPlayerNumberToDartsScorer[0]
            currentPlayerNumber = 0
            val pt = ParticipantEntity()
            pt.playerId = currentPlayerId
            hmPlayerNumberToParticipant[0] = pt
            currentRoundNumber = 1
        }

        fun setDartsThrown(dartsThrown: List<Dart>)
        {
            this.dartsThrown.clear()
            this.dartsThrown.addAll(dartsThrown)
        }
    }
}