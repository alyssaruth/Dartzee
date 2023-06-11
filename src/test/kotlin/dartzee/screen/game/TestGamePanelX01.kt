package dartzee.screen.game

import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.db.EntityName
import dartzee.db.X01FinishEntity
import dartzee.helper.AbstractTest
import dartzee.helper.AchievementSummary
import dartzee.helper.preparePlayers
import dartzee.helper.randomGuid
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.helper.wipeTable
import dartzee.`object`.Dart
import dartzee.screen.game.x01.GamePanelX01
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
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

    @Test
    fun `Should unlock the achievements correctly for a team finish, and put the right row into X01Finish`()
    {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val panel = makeX01GamePanel(team)
        val gameId = panel.gameEntity.rowId

        val darts = listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1))
        panel.addCompletedRound(darts)

        // Finish from 9
        val finishDarts = listOf(Dart(5, 1), Dart(2, 1), Dart(1, 2))
        panel.addCompletedRound(finishDarts)

        panel.updateAchievementsForFinish(1, 30)

        retrieveAchievementsForPlayer(p1.rowId).shouldContainExactly(
            AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 60, gameId)
        )

        retrieveAchievementsForPlayer(p2.rowId).shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.X01_BEST_THREE_DART_SCORE, 9, gameId),
            AchievementSummary(AchievementType.X01_BEST_FINISH, 9, gameId),
            AchievementSummary(AchievementType.X01_NO_MERCY, -1, gameId, "9"),
            AchievementSummary(AchievementType.X01_CHECKOUT_COMPLETENESS, 1, gameId),
            AchievementSummary(AchievementType.X01_BTBF, -1, gameId)
        )

        val finishes = X01FinishEntity().retrieveEntities()
        finishes.size shouldBe 1
        finishes.first().playerId shouldBe p2.rowId
    }

    @Test
    fun `Should correctly update such bad luck achievement for a team`()
    {
        val (p1, p2) = preparePlayers(2)
        val team = makeTeam(p1, p2)
        val panel = makeX01GamePanel(team, gameParams = "101")
        val gameId = panel.gameEntity.rowId

        panel.addCompletedRound(listOf(Dart(20, 3), Dart(20, 1), Dart(19, 1))) // Score 99, to put them on 2
        panel.addCompletedRound(listOf(Dart(20, 2))) // 1 for P2
        panel.addCompletedRound(listOf(Dart(20, 1)))
        panel.addCompletedRound(listOf(Dart(20, 2))) // 1 for P2
        panel.addCompletedRound(listOf(Dart(18, 2))) // 1 for P1

        retrieveAchievementsForPlayer(p1.rowId).shouldContain(
            AchievementSummary(AchievementType.X01_SUCH_BAD_LUCK, 1, gameId)
        )

        retrieveAchievementsForPlayer(p2.rowId).shouldContain(
            AchievementSummary(AchievementType.X01_SUCH_BAD_LUCK, 2, gameId)
        )
    }

    @Test
    fun `Should update the hotel inspector achievement for a unique 3-dart method of scoring of 26`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)
        val gameId = panel.gameEntity.rowId

        panel.addCompletedRound(listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))
        panel.addCompletedRound(listOf(Dart(5, 1), Dart(20, 1), Dart(1, 1)))
        panel.addCompletedRound(listOf(Dart(20, 1), Dart(3, 1), Dart(3, 1)))
        panel.addCompletedRound(listOf(Dart(20, 1), Dart(3, 2)))

        val hotelInspectorRows = retrieveAchievementsForPlayer(playerId).filter { it.achievementType == AchievementType.X01_HOTEL_INSPECTOR }
        hotelInspectorRows.shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.X01_HOTEL_INSPECTOR, -1, gameId, "20, 5, 1"),
            AchievementSummary(AchievementType.X01_HOTEL_INSPECTOR, -1, gameId, "20, 3, 3")
        )
    }

    @Test
    fun `Should not update hotel inspector achievement if board is missed, or player is bust`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId, gameParams = "101")

        panel.addCompletedRound(listOf(Dart(20, 1), Dart(3, 2), Dart(19, 0)))
        panel.addCompletedRound(listOf(Dart(20, 1), Dart(20, 1), Dart(20, 1)))
        panel.addCompletedRound(listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))

        val hotelInspectorRows = retrieveAchievementsForPlayer(playerId).filter { it.achievementType == AchievementType.X01_HOTEL_INSPECTOR }
        hotelInspectorRows.shouldBeEmpty()
    }

    @Test
    fun `Should update the chucklevision achievement for a unique 3-dart method of scoring of 69`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId)
        val gameId = panel.gameEntity.rowId

        panel.addCompletedRound(listOf(Dart(20, 3), Dart(5, 1), Dart(4, 1)))
        panel.addCompletedRound(listOf(Dart(4, 1), Dart(5, 1), Dart(20, 3)))
        panel.addCompletedRound(listOf(Dart(19, 3), Dart(7, 1), Dart(5, 1)))
        panel.addCompletedRound(listOf(Dart(20, 3), Dart(3, 3)))

        val chucklevisionRows = retrieveAchievementsForPlayer(playerId).filter { it.achievementType == AchievementType.X01_CHUCKLEVISION }
        chucklevisionRows.shouldContainExactlyInAnyOrder(
            AchievementSummary(AchievementType.X01_CHUCKLEVISION, -1, gameId, "T20, 5, 4"),
            AchievementSummary(AchievementType.X01_CHUCKLEVISION, -1, gameId, "T19, 7, 5")
        )
    }

    @Test
    fun `Should not update chucklevision achievement if board is missed, or player is bust`()
    {
        val playerId = randomGuid()
        val panel = makeX01GamePanel(playerId, gameParams = "101")

        panel.addCompletedRound(listOf(Dart(20, 3), Dart(3, 3), Dart(19, 0)))
        panel.addCompletedRound(listOf(Dart(5, 1), Dart(4, 1), Dart(20, 3)))

        val chucklevisionRows = retrieveAchievementsForPlayer(playerId).filter { it.achievementType == AchievementType.X01_CHUCKLEVISION }
        chucklevisionRows.shouldBeEmpty()
    }

    private fun GamePanelX01.updateAchievementsForFinish(finishingPosition: Int, score: Int)
    {
        updateAchievementsForFinish(getPlayerStates().first(), finishingPosition, score)
    }
}