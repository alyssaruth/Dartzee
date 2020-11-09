package dartzee.screen.player

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import com.github.alexburlton.swingtest.shouldBeVisible
import com.github.alexburlton.swingtest.shouldNotBeVisible
import dartzee.achievements.AchievementType
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.golf.AchievementGolfBestGame
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.bean.PlayerAvatar
import dartzee.core.bean.ScrollTable
import dartzee.core.util.DateStatics
import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.PlayerEntity
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
        panel.getChild<PlayerAvatar>().shouldNotBeVisible()
        panel.getChild<JButton>("Edit").shouldNotBeVisible()
        panel.getChild<JButton>("Run Simulation").shouldNotBeVisible()
        panel.getChild<JButton>("Delete").shouldNotBeVisible()
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
        panel.clickChild<JButton>("Delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete Leah?")
        player.dtDeleted shouldBe DateStatics.END_OF_TIME
        managementScreen.getChild<ScrollTable>().rowCount shouldBe 1
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
        panel.clickChild<JButton>("Delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete BTBF?")
        player.dtDeleted shouldNotBe DateStatics.END_OF_TIME
        managementScreen.getChild<ScrollTable>().rowCount shouldBe 0
        PlayerEntity.retrieveForName("BTBF") shouldBe null
    }

    @Test
    fun `Should initialise correctly for a human player`()
    {
        val image = insertPlayerImage(resource = "Sid")
        val player = insertPlayer(name = "Alex", playerImageId = image.rowId, strategy = "")

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        panel.lblPlayerName.text shouldBe "Alex"
        panel.getChild<PlayerAvatar>().icon.shouldMatch(player.getAvatar()!!)
        panel.getChild<JButton>("Delete").shouldBeVisible()
        panel.getChild<JButton>("Edit").shouldNotBeVisible()
        panel.getChild<JButton>("Run Simulation").shouldNotBeVisible()
    }

    @Test
    fun `Should initialise correctly for an AI player`()
    {
        val image = insertPlayerImage(resource = "Dennis")
        val player = insertPlayer(name = "Dennis", playerImageId = image.rowId, strategy = "foo")

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        panel.lblPlayerName.text shouldBe "Dennis"
        panel.getChild<PlayerAvatar>().icon.shouldMatch(player.getAvatar()!!)
        panel.getChild<JButton>("Delete").shouldBeVisible()
        panel.getChild<JButton>("Edit").shouldBeVisible()
        panel.getChild<JButton>("Run Simulation").shouldBeVisible()
    }

    @Test
    fun `Should handle a player with 0 games or achievements`()
    {
        val player = insertPlayer()

        val panel = PlayerManagementPanel()
        panel.refresh(player)


        val x01Button = panel.getChild<PlayerStatsButton> { it.text.contains("X01") }
        x01Button.isEnabled shouldBe false
        x01Button.text.shouldContain("Played: </b> 0")
        x01Button.text.shouldContain("Best game: </b> -")

        val achievementButton = panel.getChild<PlayerAchievementsButton>()
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

        val x01Button = panel.getChild<PlayerStatsButton> { it.text.contains("X01") }
        x01Button.isEnabled shouldBe true
        x01Button.text.shouldContain("Played: </b> 2")

        val golfButton = panel.getChild<PlayerStatsButton> { it.text.contains("Golf") }
        golfButton.isEnabled shouldBe true
        golfButton.text.shouldContain("Played: </b> 1")

        val rtcButton = panel.getChild<PlayerStatsButton> { it.text.contains("Round the Clock") }
        rtcButton.isEnabled shouldBe false
        rtcButton.text.shouldContain("Played: </b> 0")
    }

    @Test
    fun `Should pull through a players best game per type`()
    {
        val player = insertPlayer()
        insertAchievement(playerId = player.rowId, achievementRef = AchievementType.X01_BEST_GAME, achievementCounter = 25)
        insertAchievement(playerId = player.rowId, achievementRef = AchievementType.GOLF_BEST_GAME, achievementCounter = 55)

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        val x01Button = panel.getChild<PlayerStatsButton> { it.text.contains("X01") }
        x01Button.text.shouldContain("Best game: </b> 25")

        val golfButton = panel.getChild<PlayerStatsButton> { it.text.contains("Golf") }
        golfButton.text.shouldContain("Best game: </b> 55")

        val rtcButton = panel.getChild<PlayerStatsButton> { it.text.contains("Round the Clock") }
        rtcButton.text.shouldContain("Best game: </b> -")
    }

    @Test
    fun `Should pull through a players total achievement count`()
    {
        val player = insertPlayer()

        //1 pink, 1 green = 10 total
        insertAchievement(playerId = player.rowId, achievementRef = AchievementType.X01_BEST_GAME, achievementCounter = AchievementX01BestGame().pinkThreshold)
        insertAchievement(playerId = player.rowId, achievementRef = AchievementType.GOLF_BEST_GAME, achievementCounter = AchievementGolfBestGame().greenThreshold)

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        val achievementButton = panel.getChild<PlayerAchievementsButton>()
        achievementButton.text.shouldContain("10 / ${getAchievementMaximum()}")
    }

    @Test
    fun `Should support editing an AI player`()
    {
        val playerManager = mockk<PlayerManager>(relaxed = true)
        InjectedThings.playerManager = playerManager

        val player = insertPlayer(strategy = "foo")

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickChild<JButton>("Edit")

        verify { playerManager.amendPlayer(player) }
    }

    @Test
    fun `Should run a simulation for a player`()
    {
        val playerManager = mockk<PlayerManager>(relaxed = true)
        InjectedThings.playerManager = playerManager

        val player = insertPlayer(strategy = "foo")

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickChild<JButton>("Run Simulation")

        verify { playerManager.runSimulation(player) }
    }

}