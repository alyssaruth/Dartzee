package dartzee.game

import dartzee.db.EntityName
import dartzee.db.PlayerEntity
import dartzee.game.state.SingleParticipant
import dartzee.game.state.TeamParticipant
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.screen.game.makeGameLaunchParams
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestGameSqlUtils : AbstractTest()
{
    @Test
    fun `Should prepare players for a non team game correctly`()
    {
        val p1 = insertPlayer(name = "Alice")
        val p2 = insertPlayer(name = "Bob")
        val p3 = insertPlayer(name = "Clara")
        val players = listOf(p1, p2, p3)
        val params = makeGameLaunchParams(players, pairMode = false)
        val g = insertGame()

        val (pt1, pt2, pt3) = prepareParticipants(g.rowId, params) as List<SingleParticipant>
        validateSingleParticipant(pt1, g.rowId, 0, p1)
        validateSingleParticipant(pt2, g.rowId, 1, p2)
        validateSingleParticipant(pt3, g.rowId, 2, p3)

        getCountFromTable(EntityName.Participant) shouldBe 3
    }

    @Test
    fun `Should prepare players for a team game correctly`()
    {
        val p1 = insertPlayer(name = "Alice")
        val p2 = insertPlayer(name = "Bob")
        val p3 = insertPlayer(name = "Clara")
        val p4 = insertPlayer(name = "David")
        val players = listOf(p1, p2, p3, p4)
        val params = makeGameLaunchParams(players, pairMode = true)
        val g = insertGame()

        val (pt1, pt2) = prepareParticipants(g.rowId, params) as List<TeamParticipant>
        validateTeam(pt1, g.rowId, 0, p1, p2)
        validateTeam(pt2, g.rowId, 1, p3, p4)

        getCountFromTable(EntityName.Team) shouldBe 2
        getCountFromTable(EntityName.Participant) shouldBe 4
    }

    private fun validateTeam(team: TeamParticipant, gameId: String, ordinal: Int, p1: PlayerEntity, p2: PlayerEntity)
    {
        val teamEntity = team.participant
        teamEntity.gameId shouldBe gameId
        teamEntity.ordinal shouldBe ordinal
        teamEntity.retrievedFromDb shouldBe true

        val (pt1, pt2) = team.individuals
        pt1.teamId shouldBe teamEntity.rowId
        pt1.gameId shouldBe gameId
        pt1.ordinal shouldBe 0
        pt1.playerId shouldBe p1.rowId
        pt1.retrievedFromDb shouldBe true

        pt2.teamId shouldBe teamEntity.rowId
        pt2.gameId shouldBe gameId
        pt2.ordinal shouldBe 1
        pt2.playerId shouldBe p2.rowId
        pt2.retrievedFromDb shouldBe true
    }

    private fun validateSingleParticipant(pt: SingleParticipant, gameId: String, ordinal: Int, player: PlayerEntity)
    {
        pt.participant.gameId shouldBe gameId
        pt.participant.playerId shouldBe player.rowId
        pt.ordinal() shouldBe ordinal
        pt.participant.teamId shouldBe ""
        pt.participant.retrievedFromDb shouldBe true
    }
}