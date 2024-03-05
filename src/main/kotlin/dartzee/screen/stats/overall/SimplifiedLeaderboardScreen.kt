package dartzee.screen.stats.overall

import dartzee.game.GameType
import dartzee.game.X01_PARTY_CONFIG
import dartzee.screen.EmbeddedScreen
import java.awt.BorderLayout

class SimplifiedLeaderboardScreen : EmbeddedScreen() {
    private val tab = LeaderboardTotalScore(GameType.X01, X01_PARTY_CONFIG.toJson())

    init {
        add(tab, BorderLayout.CENTER)
    }

    override fun getScreenName() = "Leaderboard"

    override fun initialise() {
        tab.buildTable()
    }
}
