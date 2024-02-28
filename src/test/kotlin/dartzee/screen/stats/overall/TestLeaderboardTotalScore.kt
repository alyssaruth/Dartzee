package dartzee.screen.stats.overall

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.GameParamFilterPanelX01
import dartzee.bean.SpinnerX01
import dartzee.core.bean.ScrollTable
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.X01Config
import dartzee.helper.AbstractRegistryTest
import dartzee.helper.insertFinishedParticipant
import dartzee.helper.insertFinishedTeam
import dartzee.utils.PREFERENCES_INT_LEADERBOARD_SIZE
import dartzee.utils.PreferenceUtil
import io.kotest.matchers.shouldBe
import javax.swing.JRadioButton
import org.junit.jupiter.api.Test

class TestLeaderboardTotalScore : AbstractRegistryTest() {

    override fun getPreferencesAffected() = listOf(PREFERENCES_INT_LEADERBOARD_SIZE)

    @Test
    fun `should extract the right data into rows`() {
        val gAlice = insertFinishedParticipant("Alice", GameType.X01, 50)
        val gBob = insertFinishedParticipant("Bob", GameType.X01, 35)
        val gTeam = insertFinishedTeam("Clive", "Daisy", GameType.X01, 52)

        val leaderboard = LeaderboardTotalScore(GameType.X01)
        leaderboard.buildTable()
        leaderboard.rowCount() shouldBe 3

        leaderboard.getNameAt(0) shouldBe "Bob"
        leaderboard.getScoreAt(0) shouldBe 35
        leaderboard.getGameIdAt(0) shouldBe gBob.localId

        leaderboard.getNameAt(1) shouldBe "Alice"
        leaderboard.getScoreAt(1) shouldBe 50
        leaderboard.getGameIdAt(1) shouldBe gAlice.localId

        leaderboard.getNameAt(2) shouldBe "Clive & Daisy"
        leaderboard.getScoreAt(2) shouldBe 52
        leaderboard.getGameIdAt(2) shouldBe gTeam.localId
    }

    @Test
    fun `should ignore unfinished participants`() {
        insertFinishedParticipant("Alice", GameType.X01, -1)
        insertFinishedTeam("Clive", "Daisy", GameType.X01, -1)

        val leaderboard = LeaderboardTotalScore(GameType.X01)
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 0
    }

    @Test
    fun `should ignore games of the wrong type`() {
        insertFinishedParticipant("Alice", GameType.X01, 50)
        insertFinishedParticipant("Bob", GameType.GOLF, 35, "18")

        val leaderboard = LeaderboardTotalScore(GameType.ROUND_THE_CLOCK)
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 0
    }

    @Test
    fun `should only show games with matching parameters`() {
        insertFinishedParticipant("Alice", GameType.X01, 50)
        insertFinishedParticipant(
            "Bob",
            GameType.X01,
            70,
            X01Config(701, FinishType.Doubles).toJson()
        )

        val leaderboard = LeaderboardTotalScore(GameType.X01)
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 1
        leaderboard.getNameAt(0) shouldBe "Alice"

        leaderboard.getChild<GameParamFilterPanelX01>().getChild<SpinnerX01>().value = 701
        leaderboard.rowCount() shouldBe 1
        leaderboard.getNameAt(0) shouldBe "Bob"
    }

    @Test
    fun `Should correctly sort and limit teams and individuals`() {
        PreferenceUtil.saveInt(PREFERENCES_INT_LEADERBOARD_SIZE, 2)

        insertFinishedParticipant("Alice", GameType.X01, 35)
        insertFinishedParticipant("Bob", GameType.X01, 53)
        insertFinishedParticipant("Zulu", GameType.X01, 75)

        insertFinishedTeam("Clive", "Daisy", GameType.X01, 27)
        insertFinishedTeam("Erika", "Flora", GameType.X01, 61)
        insertFinishedTeam("Xavier", "Yolanda", GameType.X01, 83)

        val leaderboard = LeaderboardTotalScore(GameType.X01)
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Clive & Daisy"
        leaderboard.getNameAt(1) shouldBe "Alice"

        leaderboard.clickChild<JRadioButton>(text = "Teams")
        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Clive & Daisy"
        leaderboard.getNameAt(1) shouldBe "Erika & Flora"

        leaderboard.clickChild<JRadioButton>(text = "Individuals")
        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Alice"
        leaderboard.getNameAt(1) shouldBe "Bob"
    }

    @Test
    fun `Should sort correctly based on game type and selection`() {
        PreferenceUtil.saveInt(PREFERENCES_INT_LEADERBOARD_SIZE, 2)

        insertFinishedParticipant("Alice", GameType.DARTZEE, 72, gameParams = "")
        insertFinishedParticipant("Bob", GameType.DARTZEE, 54, gameParams = "")
        insertFinishedTeam("Clive", "Derek", GameType.DARTZEE, 63, gameParams = "")

        val leaderboard = LeaderboardTotalScore(GameType.DARTZEE)
        leaderboard.buildTable()
        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Alice"
        leaderboard.getNameAt(1) shouldBe "Clive & Derek"

        leaderboard.clickChild<JRadioButton>(text = "Worst")
        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Bob"
        leaderboard.getNameAt(1) shouldBe "Clive & Derek"
    }

    @Test
    fun `should support filtering to AI or humans`() {
        insertFinishedParticipant("Robocop", GameType.X01, 35, ai = true)
        insertFinishedParticipant("Doakes", GameType.X01, 35, ai = false)

        insertFinishedTeam("Wall-e", "Eve", GameType.X01, 24, p1Ai = true, p2Ai = true)
        insertFinishedTeam("Deb", "Dexter", GameType.X01, 26, p1Ai = false, p2Ai = false)
        insertFinishedTeam("Mac", "C.H.E.E.S.E", GameType.X01, 26, p1Ai = false, p2Ai = true)

        val leaderboard = LeaderboardTotalScore(GameType.X01)
        leaderboard.buildTable()
        leaderboard.rowCount() shouldBe 5

        leaderboard.clickChild<JRadioButton>(text = "Human")
        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Deb & Dexter"
        leaderboard.getNameAt(1) shouldBe "Doakes"

        leaderboard.clickChild<JRadioButton>(text = "AI")
        leaderboard.rowCount() shouldBe 2
        leaderboard.getNameAt(0) shouldBe "Wall-e & Eve"
        leaderboard.getNameAt(1) shouldBe "Robocop"
    }

    private fun LeaderboardTotalScore.rowCount() = table().rowCount

    private fun LeaderboardTotalScore.getNameAt(row: Int) = table().getValueAt(row, 2)

    private fun LeaderboardTotalScore.getGameIdAt(row: Int) = table().getValueAt(row, 3)

    private fun LeaderboardTotalScore.getScoreAt(row: Int) = table().getValueAt(row, 4)

    private fun LeaderboardTotalScore.table() = getChild<ScrollTable>()
}
