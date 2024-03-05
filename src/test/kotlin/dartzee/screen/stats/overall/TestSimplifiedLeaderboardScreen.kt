package dartzee.screen.stats.overall

import com.github.alyssaburlton.swingtest.findAll
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.ScrollTable
import dartzee.game.GameType
import dartzee.game.X01_PARTY_CONFIG
import dartzee.helper.AbstractTest
import dartzee.helper.insertFinishedParticipant
import dartzee.only
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TestSimplifiedLeaderboardScreen : AbstractTest() {
    @Test
    fun `Should show just one leaderboard of the right type`() {
        val leaderboard = SimplifiedLeaderboardScreen().findAll<AbstractLeaderboard>().only()
        leaderboard.shouldBeInstanceOf<LeaderboardTotalScore>()
    }

    @Test
    fun `Should populate the leaderboard`() {
        insertFinishedParticipant("Stuart", GameType.X01, 34, X01_PARTY_CONFIG.toJson())

        val scrn = SimplifiedLeaderboardScreen()
        scrn.initialise()

        val leaderboard = scrn.getChild<LeaderboardTotalScore>()
        leaderboard.getChild<ScrollTable>().rowCount shouldBe 1
    }
}
