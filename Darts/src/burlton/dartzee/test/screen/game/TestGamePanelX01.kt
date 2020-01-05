package burlton.dartzee.test.screen.game

import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BEST_FINISH
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BTBF
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_NO_MERCY
import burlton.dartzee.code.db.*
import burlton.dartzee.code.screen.game.GamePanelX01
import burlton.dartzee.code.screen.game.scorer.DartsScorerX01
import burlton.dartzee.test.db.TestAchievementEntity
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.randomGuid
import burlton.dartzee.test.helper.wipeTable
import io.kotlintest.shouldBe
import org.junit.Test

class TestGamePanelX01: AbstractDartsTest()
{
    @Test
    fun `Should update BTBF achievement if the game was finished on D1`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(1, 2))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_BTBF, playerId)!!
        a.gameIdEarned shouldBe panel.getGameId()
    }

    @Test
    fun `Should not update BTBF achievement if the game was finished on a different double`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(2, 2))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_BTBF, playerId) shouldBe null
    }

    @Test
    fun `Should update the best finish achievement for a player`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(20, 3), Dart(20, 2))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_BEST_FINISH, playerId)!!
        a.achievementCounter shouldBe 100
        a.gameIdEarned shouldBe panel.getGameId()
    }

    @Test
    fun `Should update No Mercy achievement if the game was finished on from 3, 5, 7 or 9`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        for (i in listOf(3, 5, 7, 9))
        {
            wipeTable("Achievement")
            val darts = listOf(Dart(1, 1), Dart((i-1)/2, 2))
            Debug.append("$darts")
            panel.setDartsThrown(darts)

            panel.updateAchievementsForFinish(playerId, 1, 30)

            val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_NO_MERCY, playerId)!!
            a.gameIdEarned shouldBe panel.getGameId()
            a.achievementDetail shouldBe "$i"
        }
    }

    @Test
    fun `Should not update No Mercy achievement if the game was finished from a higher finish`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(11, 1))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_NO_MERCY, playerId) shouldBe null
    }

    @Test
    fun `Should not update No Mercy achievement if the game was finished from an even number`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(8, 1))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_NO_MERCY, playerId) shouldBe null
    }

    private class TestGamePanel(currentPlayerId: String = randomGuid())
        : GamePanelX01(TestAchievementEntity.FakeDartsScreen(), GameEntity.factoryAndSave(GAME_TYPE_X01, "501"))
    {
        init
        {
            for (i in 0..3)
            {
                val scorer = DartsScorerX01(this)
                scorer.init(PlayerEntity(), "501")
                hmPlayerNumberToDartsScorer[i] = scorer
            }

            activeScorer = hmPlayerNumberToDartsScorer[0]!!
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