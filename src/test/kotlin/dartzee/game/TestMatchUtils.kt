package dartzee.game

import dartzee.db.PlayerEntity
import dartzee.game.state.SingleParticipant
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertParticipant
import dartzee.helper.preparePlayers
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestMatchUtils : AbstractTest() {
    @Test
    fun `Should identify an incomplete FIRST_TO match`() {
        val match = insertDartsMatch(mode = MatchMode.FIRST_TO, games = 3)

        val (p1, p2) = preparePlayers(2)
        val pts = insertWinningParticipants(p1, 2) + insertWinningParticipants(p2, 1)

        matchIsComplete(match, pts) shouldBe false
    }

    @Test
    fun `Should identify a complete FIRST_TO match`() {
        val match = insertDartsMatch(mode = MatchMode.FIRST_TO, games = 4)

        val (p1, p2) = preparePlayers(2)
        val pts = insertWinningParticipants(p1, 2) + insertWinningParticipants(p2, 4)

        matchIsComplete(match, pts) shouldBe true
    }

    @Test
    fun `Should identify an incomplete POINTS match`() {
        val match = insertDartsMatch(mode = MatchMode.POINTS, games = 4)

        val (p1, p2) = preparePlayers(2)
        val pts = insertWinningParticipants(p1, 2) + insertWinningParticipants(p2, 1)

        matchIsComplete(match, pts) shouldBe false
    }

    @Test
    fun `Should identify a complete POINTS match`() {
        val match = insertDartsMatch(mode = MatchMode.POINTS, games = 4)

        val (p1, p2) = preparePlayers(2)
        val pts = insertWinningParticipants(p1, 3) + insertWinningParticipants(p2, 1)

        matchIsComplete(match, pts) shouldBe true
    }

    private fun insertWinningParticipants(
        player: PlayerEntity,
        count: Int,
    ): List<SingleParticipant> =
        (1..count).map {
            val pt = insertParticipant(playerId = player.rowId, finishingPosition = 1)
            SingleParticipant(pt)
        }
}
