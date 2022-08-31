package dartzee.screen.stats.player.golf

import dartzee.`object`.Dart
import dartzee.core.util.getSortedValues
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
class StatisticsTabGolfOptimalScorecard : AbstractStatisticsTab()
{
    private val panelMine = JPanel()
    private val panelMyScorecard = JPanel()
    private val panelOther = JPanel()
    private val panelOtherScorecard = JPanel()

    init
    {
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

    override fun populateStats()
    {
        setOtherComponentVisibility(this, panelOther)

        populateStats(filteredGames, panelMyScorecard, false)
        if (includeOtherComparison())
        {
            populateStats(filteredGamesOther, panelOtherScorecard, true)
        }
    }

    private fun populateStats(filteredGames: List<GameWrapper>, panel: JPanel, other: Boolean)
    {
        val hmHoleToBestDarts = mutableMapOf<Int, List<Dart>>()
        val hmHoleToBestGameId = mutableMapOf<Int, Long>()

        // Add fudge data so we always display something, even if there are no games
        for (i in 1..18)
        {
            hmHoleToBestDarts[i] = listOf(Dart(20, 0), Dart(20, 0), Dart(20, 0))
            hmHoleToBestGameId[i] = -1
        }

        val sortedGames = filteredGames.sortedBy { it.dtStart }
        for (game in sortedGames)
        {
            game.populateOptimalScorecardMaps(hmHoleToBestDarts, hmHoleToBestGameId)
        }

        val testId = if (other) "scorecardOther" else "scorecardMine"
        val scoreSheet = GolfStatsScorecard(0, true, testId)
        if (other)
        {
            scoreSheet.setTableForeground(Color.RED)
        }

        val rounds = hmHoleToBestDarts.values.toList()
        scoreSheet.populateTable(rounds)

        scoreSheet.addGameIds(hmHoleToBestGameId.getSortedValues())

        panel.removeAll()
        panel.add(scoreSheet, BorderLayout.CENTER)
        panel.revalidate()
        panel.repaint()
    }
}
