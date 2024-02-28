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
import dartzee.helper.DEFAULT_X01_CONFIG
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
    fun `should ignore games of the wrong type`() {
        insertFinishedParticipant("Alice", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 50)
        insertFinishedParticipant("Bob", GameType.GOLF, "18", 35)

        val leaderboard = LeaderboardTotalScore(GameType.GOLF)
        leaderboard.buildTable()

        leaderboard.rowCount() shouldBe 0
    }

    @Test
    fun `should only show games with matching parameters`() {
        insertFinishedParticipant("Alice", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 50)
        insertFinishedParticipant(
            "Bob",
            GameType.X01,
            X01Config(701, FinishType.Doubles).toJson(),
            70
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

        insertFinishedParticipant("Alice", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 35)
        insertFinishedParticipant("Bob", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 53)
        insertFinishedParticipant("Zulu", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 75)

        insertFinishedTeam("Clive", "Daisy", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 27)
        insertFinishedTeam("Erika", "Flora", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 61)
        insertFinishedTeam("Xavier", "Yolanda", GameType.X01, DEFAULT_X01_CONFIG.toJson(), 83)

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

    private fun LeaderboardTotalScore.rowCount() = table().rowCount

    private fun LeaderboardTotalScore.getNameAt(row: Int) = table().getValueAt(row, 2)

    private fun LeaderboardTotalScore.getGameIdAt(row: Int) = table().getValueAt(row, 3)

    private fun LeaderboardTotalScore.getScoreAt(row: Int) = table().getValueAt(row, 4)

    private fun LeaderboardTotalScore.table() = getChild<ScrollTable>()
}
