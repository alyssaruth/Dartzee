package dartzee.screen.reporting

import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.makeIncludedPlayerParameters
import dartzee.reporting.IncludedPlayerParameters
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestReportingPlayersTab : AbstractTest() {
    @Test
    fun `Should initialise with an empty player table`() {
        val tab = ReportingPlayersTab()
        tab.scrollTable.lblRowCount.text shouldBe "0 players"
    }

    @Test
    fun `Should show an error if trying to remove without a selected row`() {
        val tab = ReportingPlayersTab()
        tab.addPlayers(listOf(insertPlayer()))
        tab.scrollTable.selectRow(-1)

        tab.btnRemovePlayer.doClick()

        tab.scrollTable.rowCount shouldBe 1
        dialogFactory.errorsShown.shouldContainExactly("You must select player(s) to remove.")
    }

    @Test
    fun `Should remove a player and reset their included parameters`() {
        val p = insertPlayer()
        val tab = ReportingPlayersTab()
        tab.addPlayers(listOf(p))

        tab.includedPlayerPanel.enabled() shouldBe true
        tab.includedPlayerPanel.chckbxFinalScore.doClick()

        tab.btnRemovePlayer.doClick()
        tab.scrollTable.rowCount shouldBe 0

        tab.addPlayers(listOf(p))

        tab.includedPlayerPanel.chckbxFinalScore.isSelected shouldBe false
    }

    @Test
    fun `Should enable, disable and hide the player parameter panel as appropriate`() {
        val p1 = insertPlayer()
        val p2 = insertPlayer()
        val tab = ReportingPlayersTab()
        tab.includedPlayerPanel.enabled() shouldBe false

        tab.addPlayers(listOf(p1, p2))
        tab.includedPlayerPanel.enabled() shouldBe true
        tab.includedPlayerPanel.chckbxFinalScore.doClick()

        tab.scrollTable.selectRow(1)
        tab.includedPlayerPanel.enabled() shouldBe true
        tab.includedPlayerPanel.chckbxFinalScore.isSelected shouldBe false

        tab.scrollTable.selectRow(0)
        tab.includedPlayerPanel.enabled() shouldBe true
        tab.includedPlayerPanel.chckbxFinalScore.isSelected shouldBe true

        tab.scrollTable.selectRow(-1)
        tab.includedPlayerPanel.enabled() shouldBe false
        tab.includedPlayerPanel.chckbxFinalScore.isSelected shouldBe false

        tab.components.toList().shouldContain(tab.includedPlayerPanel)

        tab.rdbtnExclude.doClick()
        tab.components.toList().shouldNotContain(tab.includedPlayerPanel)

        tab.scrollTable.selectRow(0)
        tab.components.toList().shouldNotContain(tab.includedPlayerPanel)

        tab.rdbtnInclude.doClick()
        tab.components.toList().shouldContain(tab.includedPlayerPanel)
        tab.includedPlayerPanel.enabled() shouldBe true
        tab.includedPlayerPanel.chckbxFinalScore.isSelected shouldBe true
    }

    @Test
    fun `Should populate report parameters correctly with no players selected`() {
        val tab = ReportingPlayersTab()
        tab.generateReportParameters().excludedPlayers.shouldBeEmpty()
        tab.generateReportParameters().includedPlayers.shouldBeEmpty()

        tab.rdbtnExclude.doClick()
        tab.generateReportParameters().excludedPlayers.shouldBeEmpty()
        tab.generateReportParameters().includedPlayers.shouldBeEmpty()
    }

    @Test
    fun `Should populate excludeOnlyAi correctly`() {
        val tab = ReportingPlayersTab()
        tab.generateReportParameters().excludeOnlyAi shouldBe false

        tab.checkBoxExcludeOnlyAi.doClick()
        tab.generateReportParameters().excludeOnlyAi shouldBe true

        tab.checkBoxExcludeOnlyAi.doClick()
        tab.generateReportParameters().excludeOnlyAi shouldBe false
    }

    @Test
    fun `Should populate included players correctly`() {
        val playerOne = insertPlayer()
        val playerTwo = insertPlayer()

        val tab = ReportingPlayersTab()
        tab.addPlayers(listOf(playerOne, playerTwo))
        tab.includedPlayerPanel.chckbxFinalScore.doClick()

        tab.generateReportParameters().excludedPlayers.shouldBeEmpty()
        tab.generateReportParameters()
            .includedPlayers
            .shouldContainExactly(
                mapOf(
                    playerOne to IncludedPlayerParameters(listOf(), "=", 3),
                    playerTwo to makeIncludedPlayerParameters()
                )
            )
    }

    @Test
    fun `Should populate excluded players correctly`() {
        val playerOne = insertPlayer()
        val playerTwo = insertPlayer()

        val tab = ReportingPlayersTab()
        tab.addPlayers(listOf(playerOne, playerTwo))
        tab.includedPlayerPanel.chckbxFinalScore.doClick()
        tab.rdbtnExclude.doClick()

        tab.generateReportParameters().excludedPlayers.shouldContainExactly(playerOne, playerTwo)
        tab.generateReportParameters().includedPlayers.shouldBeEmpty()
    }

    @Test
    fun `Should validate all the included player parameters`() {
        val playerOne = insertPlayer(name = "Alice")
        val playerTwo = insertPlayer(name = "Bob")

        val tab = ReportingPlayersTab()
        tab.addPlayers(listOf(playerOne, playerTwo))

        // Make both invalid
        tab.includedPlayerPanel.chckbxPosition.doClick()
        tab.scrollTable.selectRow(1)
        tab.includedPlayerPanel.chckbxPosition.doClick()

        tab.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly(
            "You must select at least one finishing position for player Alice"
        )

        dialogFactory.errorsShown.clear()
        tab.scrollTable.selectRow(0)
        tab.includedPlayerPanel.chckbxPosition.doClick()
        tab.valid() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly(
            "You must select at least one finishing position for player Bob"
        )

        dialogFactory.errorsShown.clear()
        tab.scrollTable.selectRow(1)
        tab.includedPlayerPanel.chckbxPosition.doClick()
        tab.valid() shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should not do any validation in exclude mode`() {
        val playerOne = insertPlayer()

        val tab = ReportingPlayersTab()
        tab.addPlayers(listOf(playerOne))

        // Make invalid, but then swap to exclude mode
        tab.includedPlayerPanel.chckbxPosition.doClick()
        tab.rdbtnExclude.doClick()

        tab.valid() shouldBe true
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    private fun PlayerParametersPanel.enabled() = chckbxFinalScore.isEnabled
}
