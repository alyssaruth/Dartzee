package dartzee.screen.player

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.findWindow
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldMatch
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import com.github.alyssaburlton.swingtest.typeText
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
import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import dartzee.helper.insertGameForPlayer
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.makeDartsModel
import dartzee.screen.HumanConfigurationDialog
import dartzee.screen.ScreenCache
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.ai.AISimulationSetupDialog
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.JTextField
import org.junit.jupiter.api.Test

class TestPlayerManagementPanel : AbstractTest() {
    @Test
    fun `Should clear down when refreshed with a null player`() {
        val player = insertPlayer()
        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.refresh(null)

        panel.lblPlayerName.text shouldBe ""
        panel.getChild<PlayerAvatar>().shouldNotBeVisible()
        panel.getChild<JButton>(text = "Edit").shouldNotBeVisible()
        panel.getChild<JButton>(text = "Run Simulation").shouldNotBeVisible()
        panel.getChild<JButton>(text = "Delete").shouldNotBeVisible()
        panel.getAllChildComponentsForType<PlayerSummaryButton>().shouldBeEmpty()
    }

    @Test
    fun `Should not delete a player if cancelled`() {
        val player = insertPlayer(name = "Leah")
        val managementScreen = ScreenCache.get<PlayerManagementScreen>()
        managementScreen.initialise()

        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickChild<JButton>(text = "Delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete Leah?")
        player.dtDeleted shouldBe DateStatics.END_OF_TIME
        managementScreen.getChild<ScrollTable>().rowCount shouldBe 1
        PlayerEntity.retrieveForName("Leah") shouldNotBe null
    }

    @Test
    fun `Should delete a player and update management screen`() {
        val player = insertPlayer(name = "BTBF")
        val managementScreen = ScreenCache.get<PlayerManagementScreen>()
        managementScreen.initialise()

        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickChild<JButton>(text = "Delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete BTBF?")
        player.dtDeleted shouldNotBe DateStatics.END_OF_TIME
        managementScreen.getChild<ScrollTable>().rowCount shouldBe 0
        PlayerEntity.retrieveForName("BTBF") shouldBe null
    }

    @Test
    fun `Should initialise correctly for a human player`() {
        val image = insertPlayerImage(resource = "Sid")
        val player = insertPlayer(name = "Alex", playerImageId = image.rowId, strategy = "")

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        panel.lblPlayerName.text shouldBe "Alex"
        panel.getChild<PlayerAvatar>().icon.shouldMatch(player.getAvatar())
        panel.getChild<JButton>(text = "Delete").shouldBeVisible()
        panel.getChild<JButton>(text = "Edit").shouldBeVisible()
        panel.getChild<JButton>(text = "Run Simulation").shouldNotBeVisible()
    }

    @Test
    fun `Should initialise correctly for an AI player`() {
        val image = insertPlayerImage(resource = "Dennis")
        val player = insertPlayer(name = "Dennis", playerImageId = image.rowId, strategy = "foo")

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        panel.lblPlayerName.text shouldBe "Dennis"
        panel.getChild<PlayerAvatar>().icon.shouldMatch(player.getAvatar())
        panel.getChild<JButton>(text = "Delete").shouldBeVisible()
        panel.getChild<JButton>(text = "Edit").shouldBeVisible()
        panel.getChild<JButton>(text = "Run Simulation").shouldBeVisible()
    }

    @Test
    fun `Should handle a player with 0 games or achievements`() {
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
    fun `Should pull through total games played by type`() {
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
    fun `Should pull through a players best game per type`() {
        val player = insertPlayer()
        insertAchievement(
            playerId = player.rowId,
            type = AchievementType.X01_BEST_GAME,
            achievementCounter = 25,
        )
        insertAchievement(
            playerId = player.rowId,
            type = AchievementType.GOLF_BEST_GAME,
            achievementCounter = 55,
        )

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
    fun `Should pull through a players total achievement count`() {
        val player = insertPlayer()

        // 1 pink, 1 green = 10 total
        insertAchievement(
            playerId = player.rowId,
            type = AchievementType.X01_BEST_GAME,
            achievementCounter = AchievementX01BestGame().pinkThreshold,
        )
        insertAchievement(
            playerId = player.rowId,
            type = AchievementType.GOLF_BEST_GAME,
            achievementCounter = AchievementGolfBestGame().greenThreshold,
        )

        val panel = PlayerManagementPanel()
        panel.refresh(player)

        val achievementButton = panel.getChild<PlayerAchievementsButton>()
        achievementButton.text.shouldContain("10 / ${getAchievementMaximum()}")
    }

    @Test
    fun `Should support editing a human player`() {
        val player = insertPlayer(name = "Old name")

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickChild<JButton>(text = "Edit")

        val dlg = findWindow<HumanConfigurationDialog>()
        dlg.shouldNotBeNull()

        dlg.getChild<JTextField>("nameField").typeText("New name")
        dlg.clickOk()
        flushEdt()

        panel.lblPlayerName.text shouldBe "New name"
    }

    @Test
    fun `Should support editing an AI player`() {
        val player = insertPlayer(model = makeDartsModel(), name = "Old name")

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickChild<JButton>(text = "Edit")

        val dlg = findWindow<AIConfigurationDialog>()
        dlg.shouldNotBeNull()

        dlg.getChild<JTextField>("nameField").typeText("New name")
        dlg.clickOk()
        flushEdt()

        panel.lblPlayerName.text shouldBe "New name"
    }

    @Test
    fun `Should run a simulation for a player`() {
        val player = insertPlayer(makeDartsModel())

        val panel = PlayerManagementPanel()
        panel.refresh(player)
        panel.clickChild<JButton>(text = "Run Simulation")

        findWindow<AISimulationSetupDialog>().shouldNotBeNull()
    }
}
