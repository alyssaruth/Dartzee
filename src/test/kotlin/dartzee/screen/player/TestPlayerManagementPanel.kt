package dartzee.screen.player

import dartzee.achievements.ACHIEVEMENT_REF_GOLF_BEST_GAME
import dartzee.achievements.ACHIEVEMENT_REF_X01_BEST_GAME
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.golf.AchievementGolfBestGame
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.bean.PlayerAvatar
import dartzee.clickComponent
import dartzee.core.bean.ScrollTable
import dartzee.core.util.DateStatics
import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.PlayerEntity
import dartzee.findComponent
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.player.PlayerManager
import dartzee.screen.ScreenCache
import dartzee.shouldMatch
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import javax.swing.JButton
import javax.swing.JOptionPane

class TestPlayerManagementPanel: AbstractTest()
{
    @Test
    fun `Should clear down when refreshed with a null player`()
    {
        val player = insertPlayer()
        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.refresh(null)

        panel.lblPlayerName.text shouldBe ""
        panel.findComponent<PlayerAvatar>().isVisible shouldBe false
        panel.findComponent<JButton>("Edit").isVisible shouldBe false
        panel.findComponent<JButton>("Run Simulation").isVisible shouldBe false
        panel.findComponent<JButton>("Delete").isVisible shouldBe false
        panel.getAllChildComponentsForType<PlayerSummaryButton>().shouldBeEmpty()
    }

    @Test
    fun `Should not delete a player if cancelled`()
    {
        val player = insertPlayer(name = "Leah")
        val managementScreen = ScreenCache.get<PlayerManagementScreen>()
        managementScreen.initialise()

        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickComponent<JButton>("Delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete Leah?")
        player.dtDeleted shouldBe DateStatics.END_OF_TIME
        managementScreen.findComponent<ScrollTable>().rowCount shouldBe 1
        PlayerEntity.retrieveForName("Leah") shouldNotBe null
    }

    @Test
    fun `Should delete a player and update management screen`()
    {
        val player = insertPlayer(name = "BTBF")
        val managementScreen = ScreenCache.get<PlayerManagementScreen>()
        managementScreen.initialise()

        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickComponent<JButton>("Delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete BTBF?")
        player.dtDeleted shouldNotBe DateStatics.END_OF_TIME
        managementScreen.findComponent<ScrollTable>().rowCount shouldBe 0
        PlayerEntity.retrieveForName("BTBF") shouldBe null
    }

    @Test
    fun `Should initialise correctly for a human player`()
    {
        val image = insertPlayerImage(resource = "Sid")
        val player = insertPlayer(name = "Alex", playerImageId = image.rowId, strategy = -1)

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        panel.lblPlayerName.text shouldBe "Alex"
        panel.findComponent<PlayerAvatar>().icon.shouldMatch(player.getAvatar())
        panel.findComponent<JButton>("Delete").isVisible shouldBe true
        panel.findComponent<JButton>("Edit").isVisible shouldBe false
        panel.findComponent<JButton>("Run Simulation").isVisible shouldBe false
    }

    @Test
    fun `Should initialise correctly for an AI player`()
    {
        val image = insertPlayerImage(resource = "Dennis")
        val player = insertPlayer(name = "Dennis", playerImageId = image.rowId, strategy = 2)

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        panel.lblPlayerName.text shouldBe "Dennis"
        panel.findComponent<PlayerAvatar>().icon.shouldMatch(player.getAvatar())
        panel.findComponent<JButton>("Delete").isVisible shouldBe true
        panel.findComponent<JButton>("Edit").isVisible shouldBe true
        panel.findComponent<JButton>("Run Simulation").isVisible shouldBe true
    }

    @Test
    fun `Should handle a player with 0 games or achievements`()
    {
        val player = insertPlayer()

        val panel = PlayerManagementPanel()
        panel.refresh(player)


        val x01Button = panel.findComponent<PlayerStatsButton>("X01")
        x01Button.isEnabled shouldBe false
        x01Button.text.shouldContain("Played: </b> 0")
        x01Button.text.shouldContain("Best game: </b> -")

        val achievementButton = panel.findComponent<PlayerAchievementsButton>()
        achievementButton.isEnabled shouldBe true
        achievementButton.text.shouldContain("0 / ${getAchievementMaximum()}")
    }

    @Test
    fun `Should pull through total games played by type`()
    {
        val player = insertPlayer()

        insertGameForPlayer(player, GameType.X01)
        insertGameForPlayer(player, GameType.X01)

        insertGameForPlayer(player, GameType.GOLF)

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        val x01Button = panel.findComponent<PlayerStatsButton>("X01")
        x01Button.isEnabled shouldBe true
        x01Button.text.shouldContain("Played: </b> 2")

        val golfButton = panel.findComponent<PlayerStatsButton>("Golf")
        golfButton.isEnabled shouldBe true
        golfButton.text.shouldContain("Played: </b> 1")

        val rtcButton = panel.findComponent<PlayerStatsButton>("Round the Clock")
        rtcButton.isEnabled shouldBe false
        rtcButton.text.shouldContain("Played: </b> 0")
    }

    @Test
    fun `Should pull through a players best game per type`()
    {
        val player = insertPlayer()
        insertAchievement(playerId = player.rowId, achievementRef = ACHIEVEMENT_REF_X01_BEST_GAME, achievementCounter = 25)
        insertAchievement(playerId = player.rowId, achievementRef = ACHIEVEMENT_REF_GOLF_BEST_GAME, achievementCounter = 55)

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        val x01Button = panel.findComponent<PlayerStatsButton>("X01")
        x01Button.text.shouldContain("Best game: </b> 25")

        val golfButton = panel.findComponent<PlayerStatsButton>("Golf")
        golfButton.text.shouldContain("Best game: </b> 55")

        val rtcButton = panel.findComponent<PlayerStatsButton>("Round the Clock")
        rtcButton.text.shouldContain("Best game: </b> -")
    }

    @Test
    fun `Should pull through a players total achievement count`()
    {
        val player = insertPlayer()

        //1 pink, 1 green = 10 total
        insertAchievement(playerId = player.rowId, achievementRef = ACHIEVEMENT_REF_X01_BEST_GAME, achievementCounter = AchievementX01BestGame().pinkThreshold)
        insertAchievement(playerId = player.rowId, achievementRef = ACHIEVEMENT_REF_GOLF_BEST_GAME, achievementCounter = AchievementGolfBestGame().greenThreshold)

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        val achievementButton = panel.findComponent<PlayerAchievementsButton>()
        achievementButton.text.shouldContain("10 / ${getAchievementMaximum()}")
    }

    @Test
    fun `Should support editing an AI player`()
    {
        val playerManager = mockk<PlayerManager>(relaxed = true)
        InjectedThings.playerManager = playerManager

        val player = insertPlayer(strategy = 2)

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickComponent<JButton>("Edit")

        verify { playerManager.amendPlayer(player) }
    }

    @Test
    fun `Should run a simulation for a player`()
    {
        val playerManager = mockk<PlayerManager>(relaxed = true)
        InjectedThings.playerManager = playerManager

        val player = insertPlayer(strategy = 2)

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickComponent<JButton>("Run Simulation")

        verify { playerManager.runSimulation(player) }
    }

}