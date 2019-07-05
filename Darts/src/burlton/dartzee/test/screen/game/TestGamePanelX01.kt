package burlton.dartzee.test.screen.game

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BEST_FINISH
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_BTBF
import burlton.dartzee.code.db.*
import burlton.dartzee.code.screen.game.DartsScorerX01
import burlton.dartzee.code.screen.game.GamePanelX01
import burlton.dartzee.test.db.TestAchievementEntity
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.randomGuid
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

    private class TestGamePanel(currentPlayerId: String = randomGuid()): GamePanelX01(TestAchievementEntity.FakeDartsScreen())
    {
        init
        {
            for (i in 0..3)
            {
                val scorer = DartsScorerX01()
                scorer.init(PlayerEntity(), "501")
                hmPlayerNumberToDartsScorer[i] = scorer
            }

            activeScorer = hmPlayerNumberToDartsScorer[0]
            currentPlayerNumber = 0
            val pt = ParticipantEntity()
            pt.playerId = currentPlayerId
            hmPlayerNumberToParticipant[0] = pt
            currentRoundNumber = 1

            gameEntity = GameEntity.factoryAndSave(GAME_TYPE_X01, "501")
        }

        fun setDartsThrown(dartsThrown: List<Dart>)
        {
            this.dartsThrown.addAll(dartsThrown)
        }
    }
}