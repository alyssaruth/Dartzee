package dartzee.game

import dartzee.core.helper.DeterministicCollectionShuffler
import dartzee.core.util.CollectionShuffler
import dartzee.core.util.InjectedCore
import dartzee.db.EntityName
import dartzee.db.PlayerEntity
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.SingleParticipant
import dartzee.game.state.TeamParticipant
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestGameSqlUtils : AbstractTest()
{
    @BeforeEach
    fun beforeEach()
    {
        InjectedCore.collectionShuffler = CollectionShuffler()
    }

    @Test
    fun `Should prepare and load players for a non team game correctly`()
    {
        val players = preparePlayers(3)
        val (p1, p2, p3) = players
        val g = insertGame()

        val (pt1, pt2, pt3) = prepareParticipants(g.rowId, players, false)
        validateSingleParticipant(pt1, g.rowId, 0, p1)
        validateSingleParticipant(pt2, g.rowId, 1, p2)
        validateSingleParticipant(pt3, g.rowId, 2, p3)

        getCountFromTable(EntityName.Participant) shouldBe 3

        val (loadedPt1, loadedPt2, loadedPt3) = loadParticipants(g.rowId)
        validateSingleParticipant(loadedPt1, g.rowId, 0, p1)
        validateSingleParticipant(loadedPt2, g.rowId, 1, p2)
        validateSingleParticipant(loadedPt3, g.rowId, 2, p3)
    }

    @Test
    fun `Should prepare and load players for a team game correctly`()
    {
        val players = preparePlayers(4)
        val (p1, p2, p3, p4) = players
        val g = insertGame()

        val (pt1, pt2) = prepareParticipants(g.rowId, players, true)
        validateTeam(pt1, g.rowId, 0, p1, p2)
        validateTeam(pt2, g.rowId, 1, p3, p4)

        getCountFromTable(EntityName.Team) shouldBe 2
        getCountFromTable(EntityName.Participant) shouldBe 4

        val (loadedPt1, loadedPt2) = loadParticipants(g.rowId)
        validateTeam(loadedPt1, g.rowId, 0, p1, p2)
        validateTeam(loadedPt2, g.rowId, 1, p3, p4)
    }

    @Test
    fun `Should create a single participant if there are an odd number of players`()
    {
        val players = preparePlayers(3)
        val (p1, p2, p3) = players
        val g = insertGame()

        val (pt1, pt2) = prepareParticipants(g.rowId, players, true)
        validateTeam(pt1, g.rowId, 0, p1, p2)
        validateSingleParticipant(pt2, g.rowId, 1, p3)

        getCountFromTable(EntityName.Team) shouldBe 1
        getCountFromTable(EntityName.Participant) shouldBe 3

        val (loadedPt1, loadedPt2) = loadParticipants(g.rowId)
        validateTeam(loadedPt1, g.rowId, 0, p1, p2)
        validateSingleParticipant(loadedPt2, g.rowId, 1, p3)
    }

    @Test
    fun `Should prepare next participants correctly for a 2 player game`()
    {
        val players = preparePlayers(2)
        val (p1, p2) = players
        val participants = prepareParticipants(insertGame().rowId, players, false)
        val g2 = insertGame(matchOrdinal = 2)
        val g3 = insertGame(matchOrdinal = 3)

        val (pt2_1, pt2_2) = prepareNextParticipants(participants, g2)
        validateSingleParticipant(pt2_1, g2.rowId, 0, p2)
        validateSingleParticipant(pt2_2, g2.rowId, 1, p1)

        val (pt3_1, pt3_2) = prepareNextParticipants(participants, g3)
        validateSingleParticipant(pt3_1, g3.rowId, 0, p1)
        validateSingleParticipant(pt3_2, g3.rowId, 1, p2)
    }

    @Test
    fun `Should prepare next participants correctly for a 2 team game`()
    {
        val players = preparePlayers(4)
        val (p1, p2, p3, p4) = players

        val participants = prepareParticipants(insertGame().rowId, players, true)
        val g2 = insertGame(matchOrdinal = 2)
        val g3 = insertGame(matchOrdinal = 3)

        val (pt2_1, pt2_2) = prepareNextParticipants(participants, g2)
        validateTeam(pt2_1, g2.rowId, 0, p4, p3)
        validateTeam(pt2_2, g2.rowId, 1, p2, p1)

        val (pt3_1, pt3_2) = prepareNextParticipants(participants, g3)
        validateTeam(pt3_1, g3.rowId, 0, p1, p2)
        validateTeam(pt3_2, g3.rowId, 1, p3, p4)
    }

    @Test
    fun `Should prepare next participants correctly for a game with more than 2 teams`()
    {
        InjectedCore.collectionShuffler = DeterministicCollectionShuffler()

        val players = preparePlayers(5)
        val (p1, p2, p3, p4, p5) = players

        val participants = prepareParticipants(insertGame().rowId, players, true)
        val g2 = insertGame(matchOrdinal = 2)
        val g3 = insertGame(matchOrdinal = 3)

        val (pt2_1, pt2_2, pt2_3) = prepareNextParticipants(participants, g2)
        validateTeam(pt2_1, g2.rowId, 0, p4, p3)
        validateSingleParticipant(pt2_2, g2.rowId, 1, p5)
        validateTeam(pt2_3, g2.rowId, 2, p2, p1)

        val (loadedPt2_1, loadedPt2_2, loadedPt2_3) = loadParticipants(g2.rowId)
        validateTeam(loadedPt2_1, g2.rowId, 0, p4, p3)
        validateSingleParticipant(loadedPt2_2, g2.rowId, 1, p5)
        validateTeam(loadedPt2_3, g2.rowId, 2, p2, p1)

        val (pt3_1, pt3_2, pt3_3) = prepareNextParticipants(participants, g3)
        validateTeam(pt3_1, g3.rowId, 0, p3, p4)
        validateSingleParticipant(pt3_2, g3.rowId, 1, p5)
        validateTeam(pt3_3, g3.rowId, 2, p1, p2)
    }

    private fun preparePlayers(count: Int): List<PlayerEntity>
    {
        val p1 = insertPlayer(name = "Alice")
        val p2 = insertPlayer(name = "Bob")
        val p3 = insertPlayer(name = "Clara")
        val p4 = insertPlayer(name = "David")
        val p5 = insertPlayer(name = "Ellie")

        return listOf(p1, p2, p3, p4, p5).subList(0, count)
    }

    private fun validateTeam(team: IWrappedParticipant, gameId: String, ordinal: Int, p1: PlayerEntity, p2: PlayerEntity)
    {
        team.shouldBeInstanceOf<TeamParticipant> {
            val teamEntity = it.participant
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
    }

    private fun validateSingleParticipant(pt: IWrappedParticipant, gameId: String, ordinal: Int, player: PlayerEntity)
    {
        pt.shouldBeInstanceOf<SingleParticipant>() {
            it.participant.gameId shouldBe gameId
            it.participant.playerId shouldBe player.rowId
            it.ordinal() shouldBe ordinal
            it.participant.teamId shouldBe ""
            it.participant.retrievedFromDb shouldBe true
        }
    }
}