package dartzee.screen.game

import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.db.X01FinishEntity
import dartzee.helper.AbstractTest
import dartzee.helper.randomGuid
import dartzee.helper.wipeTable
import dartzee.`object`.Dart
import dartzee.screen.game.x01.GamePanelX01
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestGamePanelX01: AbstractTest()
{
    @Test
    fun `Should update BTBF achievement if the game was finished on D1`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)

        val darts = listOf(Dart(1, 2))
        panel.addCompletedRound(darts)
        panel.updateAchievementsForFinish(1, 30)

        val a = AchievementEntity.retrieveAchievement(AchievementType.X01_BTBF, playerId)!!
        a.gameIdEarned shouldBe panel.getGameId()
    }

    @Test
    fun `Should not update BTBF achievement if the game was finished on a different double`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)

        val darts = listOf(Dart(2, 2))
        panel.addCompletedRound(darts)

        panel.updateAchievementsForFinish(1, 30)

        AchievementEntity.retrieveAchievement(AchievementType.X01_BTBF, playerId) shouldBe null
    }

    @Test
    fun `Should update the best finish achievement for a player`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)

        val darts = listOf(Dart(20, 3), Dart(20, 2))
        panel.addCompletedRound(darts)

        panel.updateAchievementsForFinish(1, 30)

        val a = AchievementEntity.retrieveAchievement(AchievementType.X01_BEST_FINISH, playerId)!!
        a.achievementCounter shouldBe 100
        a.gameIdEarned shouldBe panel.getGameId()
    }

    @Test
    fun `Should update X01Finish table`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)

        val darts = listOf(Dart(20, 3), Dart(20, 2))
        panel.addCompletedRound(darts)
        panel.updateAchievementsForFinish(1, 30)

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
        val panel = makeX01GamePanel(playerId)

        for (i in listOf(3, 5, 7, 9))
        {
            wipeTable(EntityName.Achievement)
            val darts = listOf(Dart(1, 1), Dart((i-1)/2, 2))
            panel.addCompletedRound(darts)

            panel.updateAchievementsForFinish(1, 30)

            val a = AchievementEntity.retrieveAchievement(AchievementType.X01_NO_MERCY, playerId)!!
            a.gameIdEarned shouldBe panel.getGameId()
            a.achievementDetail shouldBe "$i"
        }
    }

    @Test
    fun `Should not update No Mercy achievement if the game was finished from a higher finish`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)

        val darts = listOf(Dart(11, 1))
        panel.addCompletedRound(darts)

        panel.updateAchievementsForFinish(1, 30)

        AchievementEntity.retrieveAchievement(AchievementType.X01_NO_MERCY, playerId) shouldBe null
    }

    @Test
    fun `Should not update No Mercy achievement if the game was finished from an even number`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)

        val darts = listOf(Dart(8, 1))
        panel.addCompletedRound(darts)

        panel.updateAchievementsForFinish(1, 30)

        AchievementEntity.retrieveAchievement(AchievementType.X01_NO_MERCY, playerId) shouldBe null
    }

    private fun GamePanelX01.updateAchievementsForFinish(finishingPosition: Int, score: Int)
    {
        updateAchievementsForFinish(getPlayerStates().first(), finishingPosition, score)
    }
}