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

        teamOne.getParticipantName().value shouldBe "Alyssa & Leah"
        teamTwo.getParticipantName().value shouldBe "Leah & Alyssa"
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