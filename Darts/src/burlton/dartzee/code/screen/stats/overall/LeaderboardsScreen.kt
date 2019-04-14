package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import java.awt.BorderLayout
import javax.swing.JTabbedPane
import javax.swing.SwingConstants

class LeaderboardsScreen : EmbeddedScreen()
{
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val tabs = getTabs()

    init
    {
        add(tabbedPane, BorderLayout.CENTER)

        tabs.forEach{
            tabbedPane.addTab(it.getTabName(), null, it, null)
        }
    }

    override fun getScreenName() = "Leaderboards"

    override fun initialise()
    {
        tabs.forEach{
            it.buildTable()
        }
    }

    private fun getTabs(): List<AbstractLeaderboard>
    {
        val tabs = mutableListOf<AbstractLeaderboard>()

        GameEntity.getAllGameTypes().forEach{
            tabs.add(LeaderboardTotalScore(it))
        }

        tabs.add(LeaderboardTopX01Finishes())
        tabs.add(LeaderboardAchievements())

        return tabs
    }
}
