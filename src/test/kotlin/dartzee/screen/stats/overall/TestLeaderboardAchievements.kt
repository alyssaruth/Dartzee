package dartzee.screen.stats.overall

import dartzee.achievements.AchievementType
import dartzee.achievements.DummyAchievementTotal
import dartzee.achievements.getAllAchievements
import dartzee.achievements.golf.AchievementGolfBestGame
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import dartzee.helper.insertPlayer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestLeaderboardAchievements : AbstractTest() {
    @Test
    fun `Combo box should be disabled by default`() {
        val leaderboard = LeaderboardAchievements()
        leaderboard.cbSpecificAchievement.isSelected shouldBe false
        leaderboard.comboBox.isEnabled shouldBe false
    }

    @Test
    fun `Should enable combo box based on checkbox`() {
        val leaderboard = LeaderboardAchievements()

        leaderboard.cbSpecificAchievement.isSelected = true
        leaderboard.actionPerformed(null)
        leaderboard.comboBox.isEnabled shouldBe true
    }

    @Test
    fun `Should initialise combobox with all achievements`() {
        val leaderboard = LeaderboardAchievements()
        leaderboard.comboBox.itemCount shouldBe getAllAchievements().size
    }

    @Test
    fun `Should show overall achievement progress by default`() {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val bestFinish = AchievementX01BestFinish()
        val bestGolfGame = AchievementGolfBestGame()

        insertAchievement(
            type = AchievementType.X01_BEST_FINISH,
            achievementCounter = bestFinish.pinkThreshold,
            playerId = alice.rowId
        )

        insertAchievement(
            type = AchievementType.GOLF_BEST_GAME,
            achievementCounter = bestGolfGame.blueThreshold,
            playerId = alice.rowId
        )

        insertAchievement(
            type = AchievementType.X01_BEST_FINISH,
            achievementCounter = bestFinish.greenThreshold,
            playerId = bob.rowId
        )

        val leaderboard = LeaderboardAchievements()
        leaderboard.buildTable()

        val table = leaderboard.table
        table.rowCount shouldBe 2

        val achievementAlice = table.getValueAt(0, ACHIEVEMENT_COLUMN_IX) as DummyAchievementTotal
        achievementAlice.attainedValue shouldBe 11

        val achievementBob = table.getValueAt(1, ACHIEVEMENT_COLUMN_IX) as DummyAchievementTotal
        achievementBob.attainedValue shouldBe 4
    }

    @Test
    fun `Should respond to changing player filters`() {
        val alice = insertPlayer(name = "Alice", strategy = "")
        val bob = insertPlayer(name = "Bob", strategy = "foo")

        val bestFinish = AchievementX01BestFinish()
        insertAchievement(
            type = AchievementType.X01_BEST_FINISH,
            achievementCounter = bestFinish.pinkThreshold,
            playerId = alice.rowId
        )

        insertAchievement(
            type = AchievementType.X01_BEST_FINISH,
            achievementCounter = bestFinish.greenThreshold,
            playerId = bob.rowId
        )

        val leaderboard = LeaderboardAchievements()
        leaderboard.buildTable()
        leaderboard.table.rowCount shouldBe 2

        leaderboard.panelPlayerFilters.rdbtnHuman.doClick()
        leaderboard.table.rowCount shouldBe 1
        (leaderboard.table.getValueAt(0, 2) as PlayerEntity).rowId shouldBe alice.rowId

        leaderboard.panelPlayerFilters.rdbtnAi.doClick()
        leaderboard.table.rowCount shouldBe 1
        (leaderboard.table.getValueAt(0, 2) as PlayerEntity).rowId shouldBe bob.rowId
    }

    @Test
    fun `Should include rankings`() {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")
        val clive = insertPlayer(name = "Clive")

        val bestFinish = AchievementX01BestFinish()
        insertAchievement(
            type = AchievementType.X01_BEST_FINISH,
            achievementCounter = bestFinish.pinkThreshold,
            playerId = alice.rowId
        )

        insertAchievement(
            type = AchievementType.X01_BEST_FINISH,
            achievementCounter = bestFinish.greenThreshold,
            playerId = bob.rowId
        )

        insertAchievement(
            type = AchievementType.X01_BEST_FINISH,
            achievementCounter = bestFinish.pinkThreshold,
            playerId = clive.rowId
        )

        val leaderboard = LeaderboardAchievements()
        leaderboard.buildTable()
        leaderboard.table.rowCount shouldBe 3

        leaderboard.table.getValueAt(0, 0) shouldBe 1
        leaderboard.table.getValueAt(1, 0) shouldBe 1
        leaderboard.table.getValueAt(2, 0) shouldBe 3
    }
}
