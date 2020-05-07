package dartzee.screen.player

import dartzee.bean.PlayerAvatar
import dartzee.clickComponent
import dartzee.core.bean.ScrollTable
import dartzee.core.util.DateStatics
import dartzee.core.util.getAllChildComponentsForType
import dartzee.db.PlayerEntity
import dartzee.findComponent
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.insertPlayerImage
import dartzee.helper.shouldMatch
import dartzee.screen.ScreenCache
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
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
}