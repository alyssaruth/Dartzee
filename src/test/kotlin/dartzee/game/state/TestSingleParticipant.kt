package dartzee.game.state

import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.theme.OKTOBERFEST
import dartzee.theme.Themes
import dartzee.utils.InjectedThings
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSingleParticipant : AbstractTest() {
    @Test
    fun `Should always return details about the individual participant`() {
        val player = insertPlayer(name = "Alyssa")
        val pt = insertParticipant(playerId = player.rowId)

        val singlePt = SingleParticipant(pt)
        singlePt.individuals shouldBe listOf(pt)
        singlePt.getIndividual(1) shouldBe pt
        singlePt.getIndividual(2) shouldBe pt
        singlePt.getUniqueParticipantName().value shouldBe "Alyssa"
        singlePt.getParticipantName() shouldBe "Alyssa"
        singlePt.getParticipantNameHtml(false) shouldBe
            "<html><p style=\"text-align:center;\"><font face=\"Trebuchet MS\">Alyssa</font></p></html>"
        singlePt.getParticipantNameHtml(true, pt) shouldBe
            "<html><p style=\"text-align:center;\"><font face=\"Trebuchet MS\"><b>Alyssa</b></font></p></html>"
    }

    @Test
    fun `Should take theming into account for the font`() {
        InjectedThings.theme = Themes.OKTOBERFEST

        val player = insertPlayer(name = "Alyssa")
        val pt = insertParticipant(playerId = player.rowId)

        val singlePt = SingleParticipant(pt)

        singlePt.getParticipantNameHtml(false) shouldBe
            "<html><p style=\"text-align:center;\"><font face=\"Ancient\">Alyssa</font></p></html>"
    }
}
