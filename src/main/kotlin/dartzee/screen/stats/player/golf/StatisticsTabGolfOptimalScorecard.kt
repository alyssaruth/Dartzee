package dartzee.screen.stats.player.golf

import dartzee.core.util.getSortedValues
import dartzee.`object`.Dart
import dartzee.screen.stats.player.AbstractStatisticsTab
import dartzee.stats.GameWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel

/**
 * Cherry-picks your best performance ever for each hole and assembles it into an 'ideal' scorecard.
 * Just for fun!
 */
class StatisticsTabGolfOptimalScorecard : AbstractStatisticsTab() {
    private val panelMine = JPanel()
    private val panelMyScorecard = JPanel()
    private val panelOther = JPanel()
    private val panelOtherScorecard = JPanel()

    init {
        layout = GridLayout(0, 2, 0, 0)
        add(panelMine)
        panelMine.layout = BorderLayout(0, 0)
        panelOther.layout = BorderLayout(0, 0)

        panelMine.add(panelMyScorecard)
        panelMyScorecard.layout = BorderLayout(0, 0)
        add(panelOther)
        panelOther.add(panelOtherScorecard)
        panelOtherScorecard.layout = BorderLayout(0, 0)
    }

    override fun populateStats() {
        setOtherComponentVisibility(this, panelOther)

        populateStats(filteredGames, panelMyScorecard, false)
        if (includeOtherComparison()) {
            populateStats(filteredGamesOther, panelOtherScorecard, true)
        }
    }

    private fun populateStats(filteredGames: List<GameWrapper>, panel: JPanel, other: Boolean) {
        val hmHoleToOptimalScorecard = makeOptimalScorecardStartingMap()

        val sortedGames = filteredGames.sortedBy { it.dtStart }
        for (game in sortedGames) {
            game.populateOptimalScorecardMaps(hmHoleToOptimalScorecard)
        }

        val testId = if (other) "scorecardOther" else "scorecardMine"
        val scoreSheet = GolfStatsScorecard(0, true, testId)
        if (other) {
            scoreSheet.setTableForeground(Color.RED)
        }

        val optimalValues = hmHoleToOptimalScorecard.getSortedValues()
        scoreSheet.populateTable(optimalValues.map { it.darts })
        scoreSheet.addGameIds(optimalValues.map { it.localGameId })

        panel.removeAll()
        panel.add(scoreSheet, BorderLayout.CENTER)
        panel.revalidate()
        panel.repaint()
    }
}

data class OptimalHoleStat(val darts: List<Dart>, val localGameId: Long)

fun makeOptimalScorecardStartingMap(): MutableMap<Int, OptimalHoleStat> {
    val hm = mutableMapOf<Int, OptimalHoleStat>()

    // Add fudge data so we always display something, even if there are no games
    for (i in 1..18) {
        hm[i] = OptimalHoleStat(listOf(Dart(20, 0), Dart(20, 0), Dart(20, 0)), -1)
    }

    return hm
}
