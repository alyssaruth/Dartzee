package burlton.dartzee.test.screen.stats.overall

import burlton.dartzee.code.achievements.*
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.screen.stats.overall.LeaderboardAchievements
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertAchievement
import burlton.dartzee.test.helper.insertPlayer
import burlton.dartzee.test.helper.wipeTable
import io.kotlintest.shouldBe
import org.junit.Test

class TestLeaderboardAchievements: AbstractDartsTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()

        wipeTable("Achievement")
    }

    @Test
    fun `Combo box should be disabled by default`()
    {
        val leaderboard = LeaderboardAchievements()
        leaderboard.cbSpecificAchievement.isSelected shouldBe false
        leaderboard.comboBox.isEnabled shouldBe false
    }

    @Test
    fun `Should enable combo box based on checkbox`()
    {
        val leaderboard = LeaderboardAchievements()

        leaderboard.cbSpecificAchievement.isSelected = true
        leaderboard.actionPerformed(null)
        leaderboard.comboBox.isEnabled shouldBe true
    }

    @Test
    fun `Should initialise combobox with all achievements`()
    {
        val leaderboard = LeaderboardAchievements()
        leaderboard.comboBox.itemCount shouldBe getAllAchievements().size
    }

    @Test
    fun `Should show overall achievement progress by default`()
    {
        val alice = insertPlayer(name = "Alice")
        val bob = insertPlayer(name = "Bob")

        val bestFinish = AchievementX01BestFinish()
        val bestGolfGame = AchievementGolfBestGame()

        insertAchievement(achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH,
                achievementCounter = bestFinish.pinkThreshold,
                playerId = alice.rowId)

        insertAchievement(achievementRef = ACHIEVEMENT_REF_GOLF_BEST_GAME,
                achievementCounter = bestGolfGame.blueThreshold,
                playerId = alice.rowId)

        insertAchievement(achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH,
                achievementCounter = bestFinish.greenThreshold,
                playerId = bob.rowId)


        val leaderboard = LeaderboardAchievements()
        leaderboard.buildTable()

        val table = leaderboard.table
        table.rowCount shouldBe 2

        val achievementAlice = table.getValueAt(0, 2) as DummyAchievementTotal
        achievementAlice.attainedValue shouldBe 11

        val achievementBob = table.getValueAt(1, 2) as DummyAchievementTotal
        achievementBob.attainedValue shouldBe 4
    }

    @Test
    fun `Should respond to changing player filters`()
    {
        val alice = insertPlayer(name = "Alice", strategy = -1)
        val bob = insertPlayer(name = "Bob", strategy = 1)

        val bestFinish = AchievementX01BestFinish()
        insertAchievement(achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH,
                achievementCounter = bestFinish.pinkThreshold,
                playerId = alice.rowId)

        insertAchievement(achievementRef = ACHIEVEMENT_REF_X01_BEST_FINISH,
                achievementCounter = bestFinish.greenThreshold,
                playerId = bob.rowId)

        val leaderboard = LeaderboardAchievements()
        leaderboard.buildTable()
        leaderboard.table.rowCount shouldBe 2

        leaderboard.playerFilterPanel.rdbtnHuman.isSelected = true
        leaderboard.actionPerformed(null)
        leaderboard.table.rowCount shouldBe 1
        (leaderboard.table.getValueAt(0, 1) as PlayerEntity).rowId shouldBe alice.rowId

        leaderboard.playerFilterPanel.rdbtnAi.isSelected = true
        leaderboard.actionPerformed(null)
        leaderboard.table.rowCount shouldBe 1
        (leaderboard.table.getValueAt(0, 1) as PlayerEntity).rowId shouldBe bob.rowId
    }
}
