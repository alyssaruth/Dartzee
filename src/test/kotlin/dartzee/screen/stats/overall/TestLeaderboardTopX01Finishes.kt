package dartzee.screen.stats.overall

import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.bean.ScrollTable
import dartzee.helper.AbstractTest
import dartzee.helper.insertFinishForPlayer
import dartzee.helper.insertPlayer
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings.preferenceService
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import org.junit.jupiter.api.Test

class TestLeaderboardTopX01Finishes : AbstractTest() {
    @Test
    fun `Should get the correct local game ids`() {
        val p = insertPlayer()

        val g1 = insertFinishForPlayer(p, 150)
        val g2 = insertFinishForPlayer(p, 90)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.getGameIdAt(0) shouldBe g1.localId
        leaderboard.getGameIdAt(1) shouldBe g2.localId
    }

    @Test
    fun `Should respect the preference value for the number of rows to be returned`() {
        preferenceService.save(Preferences.leaderboardSize, 2)

        val p = insertPlayer()

        insertFinishForPlayer(p, 100)
        insertFinishForPlayer(p, 150)
        insertFinishForPlayer(p, 90)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 2
        leaderboard.getScoreAt(0) shouldBe 150
        leaderboard.getScoreAt(1) shouldBe 100

        preferenceService.save(Preferences.leaderboardSize, 3)
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 3
    }

    @Test
    fun `Should respond to changing player filters, and pull through player names`() {
        val robot = insertPlayer(name = "Robot", strategy = "foo")
        val human = insertPlayer(name = "Human", strategy = "")

        insertFinishForPlayer(robot, 75)
        insertFinishForPlayer(human, 83)

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Human"
        leaderboard.getNameAt(1) shouldBe "Robot"

        leaderboard.panelPlayerFilters.rdbtnAi.doClick()
        leaderboard.rowCount() shouldBe 1
        leaderboard.getNameAt(0) shouldBe "Robot"

        leaderboard.panelPlayerFilters.rdbtnHuman.doClick()
        leaderboard.rowCount() shouldBe 1
        leaderboard.getNameAt(0) shouldBe "Human"
    }

    @Test
    fun `Should use dtCreation as a tie-breaker when there are multiple rows with the same score`() {
        val p = insertPlayer()

        val g1 = insertFinishForPlayer(p, 100, Timestamp(20))
        val g3 = insertFinishForPlayer(p, 100, Timestamp(100))
        val g2 = insertFinishForPlayer(p, 100, Timestamp(50))

        val leaderboard = LeaderboardTopX01Finishes()
        leaderboard.buildTable()

        leaderboard.getGameIdAt(0) shouldBe g1.localId
        leaderboard.getGameIdAt(1) shouldBe g2.localId
        leaderboard.getGameIdAt(2) shouldBe g3.localId
    }

    private fun LeaderboardTopX01Finishes.rowCount() = table().rowCount

    private fun LeaderboardTopX01Finishes.getNameAt(row: Int) = table().getValueAt(row, 2)

    private fun LeaderboardTopX01Finishes.getGameIdAt(row: Int) = table().getValueAt(row, 3)

    private fun LeaderboardTopX01Finishes.getScoreAt(row: Int) = table().getValueAt(row, 4)

    private fun LeaderboardTopX01Finishes.table() = getChild<ScrollTable>()
}
