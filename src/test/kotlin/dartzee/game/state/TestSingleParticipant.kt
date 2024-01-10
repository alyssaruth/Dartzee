package dartzee.game.state

import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
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
        singlePt.getParticipantNameHtml(false) shouldBe "<html>Alyssa</html>"
        singlePt.getParticipantNameHtml(true, pt) shouldBe "<html><b>Alyssa</b></html>"
    }
}
