package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.achievements.AchievementType
import dartzee.db.*
import dartzee.game.GameType
import dartzee.game.state.X01PlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.randomGuid
import dartzee.helper.wipeTable
import dartzee.screen.game.x01.GamePanelX01
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestGamePanelX01: AbstractTest()
{
    @Test
    fun `Should update BTBF achievement if the game was finished on D1`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(1, 2))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        val a = AchievementEntity.retrieveAchievement(AchievementType.X01_BTBF, playerId)!!
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

        AchievementEntity.retrieveAchievement(AchievementType.X01_BTBF, playerId) shouldBe null
    }

    @Test
    fun `Should update the best finish achievement for a player`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(20, 3), Dart(20, 2))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        val a = AchievementEntity.retrieveAchievement(AchievementType.X01_BEST_FINISH, playerId)!!
        a.achievementCounter shouldBe 100
        a.gameIdEarned shouldBe panel.getGameId()
    }

    @Test
    fun `Should update X01Finish table`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(20, 3), Dart(20, 2))
        panel.setDartsThrown(darts)
        panel.updateAchievementsForFinish(playerId, 1, 30)

        X01FinishEntity().retrieveEntities().size shouldBe 1
        val entity = X01FinishEntity().retrieveEntities().first()
        entity.playerId shouldBe playerId
        entity.gameId shouldBe panel.getGameId()
        entity.finish shouldBe 100
    }

    @Test
    fun `Should update No Mercy achievement if the game was finished on from 3, 5, 7 or 9`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        for (i in listOf(3, 5, 7, 9))
        {
            wipeTable(EntityName.Achievement)
            val darts = listOf(Dart(1, 1), Dart((i-1)/2, 2))
            panel.setDartsThrown(darts)

            panel.updateAchievementsForFinish(playerId, 1, 30)

            val a = AchievementEntity.retrieveAchievement(AchievementType.X01_NO_MERCY, playerId)!!
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

        AchievementEntity.retrieveAchievement(AchievementType.X01_NO_MERCY, playerId) shouldBe null
    }

    @Test
    fun `Should not update No Mercy achievement if the game was finished from an even number`()
    {
        val playerId = randomGuid()
        val panel = TestGamePanel(playerId)

        val darts = listOf(Dart(8, 1))
        panel.setDartsThrown(darts)

        panel.updateAchievementsForFinish(playerId, 1, 30)

        AchievementEntity.retrieveAchievement(AchievementType.X01_NO_MERCY, playerId) shouldBe null
    }

    private class TestGamePanel(currentPlayerId: String = randomGuid())
        : GamePanelX01(TestAchievementEntity.FakeDartsScreen(), GameEntity.factoryAndSave(GameType.X01, "501"), 1)
    {
        init
        {
            val player = insertPlayer(currentPlayerId)
            val scorer = assignScorer(player)

            currentPlayerNumber = 0
            val pt = ParticipantEntity()
            pt.playerId = currentPlayerId

            addState(0, X01PlayerState(501, pt), scorer)

            currentRoundNumber = 1
        }

        fun setDartsThrown(dartsThrown: List<Dart>)
        {
            getCurrentPlayerState().addCompletedRound(dartsThrown)
        }
    }
}