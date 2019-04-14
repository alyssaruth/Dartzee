package burlton.dartzee.code.screen.stats.overall

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.screen.EmbeddedScreen
import java.awt.BorderLayout
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class LeaderboardsScreen : EmbeddedScreen(), ChangeListener
{
    private val tabbedPane = JTabbedPane(SwingConstants.TOP)
    private val tabs = getTabs()

    init
    {
        add(tabbedPane, BorderLayout.CENTER)

        tabs.forEach{
            tabbedPane.addTab(it.getTabName(), null, it, null)
        }

        tabbedPane.addChangeListener(this)
    }

    override fun getScreenName() = "Leaderboards"

    override fun initialise()
    {
        tabs.first().buildTableFirstTime()
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

    override fun stateChanged(e: ChangeEvent)
    {
        val index = tabbedPane.selectedIndex
        val selectedTab = tabs[index]

        selectedTab.buildTableFirstTime()
    }
}
