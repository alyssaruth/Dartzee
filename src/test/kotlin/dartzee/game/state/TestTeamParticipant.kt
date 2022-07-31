package dartzee.game.state

import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertTeam
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestTeamParticipant: AbstractTest()
{
    @Test
    fun `Should return a deterministic team name regardless of player order`()
    {
        val p1 = insertPlayer(name = "Alyssa")
        val p2 = insertPlayer(name = "Leah")

        val pt1 = insertParticipant(playerId = p1.rowId)
        val pt2 = insertParticipant(playerId = p2.rowId)

        val teamOne = TeamParticipant(insertTeam(), listOf(pt1, pt2))
        val teamTwo = TeamParticipant(insertTeam(), listOf(pt2, pt1))

        teamOne.getUniqueParticipantName().value shouldBe "Alyssa & Leah"
        teamTwo.getUniqueParticipantName().value shouldBe "Alyssa & Leah"
    }

    @Test
    fun `Should return team name in throw order`()
    {
        val p1 = insertPlayer(name = "Alyssa")
        val p2 = insertPlayer(name = "Leah")

        val pt1 = insertParticipant(playerId = p1.rowId)
        val pt2 = insertParticipant(playerId = p2.rowId)

        val teamOne = TeamParticipant(insertTeam(), listOf(pt1, pt2))
        val teamTwo = TeamParticipant(insertTeam(), listOf(pt2, pt1))

        teamOne.getParticipantNameHtml(false) shouldBe "<html>Alyssa &#38; Leah</html>"
        teamTwo.getParticipantNameHtml(false) shouldBe "<html>Leah &#38; Alyssa</html>"
    }

    @Test
    fun `Should bold the right player when active`()
    {
        val p1 = insertPlayer(name = "Alyssa")
        val p2 = insertPlayer(name = "Leah")

        val pt1 = insertParticipant(playerId = p1.rowId)
        val pt2 = insertParticipant(playerId = p2.rowId)

        val team = TeamParticipant(insertTeam(), listOf(pt1, pt2))

        team.getParticipantNameHtml(true, pt1) shouldBe "<html><b>Alyssa</b> &#38; Leah</html>"
        team.getParticipantNameHtml(true, pt2) shouldBe "<html>Alyssa &#38; <b>Leah</b></html>"
    }

    @Test
    fun `Should return the correct participant based on round number`()
    {
        val pt1 = insertParticipant()
        val pt2 = insertParticipant()

        val team = TeamParticipant(insertTeam(), listOf(pt1, pt2))

        team.getIndividual(1) shouldBe pt1
        team.getIndividual(3) shouldBe pt1
        team.getIndividual(5) shouldBe pt1
        team.getIndividual(2) shouldBe pt2
        team.getIndividual(4) shouldBe pt2
        team.getIndividual(6) shouldBe pt2
    }
}