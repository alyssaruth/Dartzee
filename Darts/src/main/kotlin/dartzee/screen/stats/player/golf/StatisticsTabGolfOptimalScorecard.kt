package dartzee.screen.stats.player.golf

import dartzee.core.obj.HashMapList
import dartzee.`object`.Dart
import dartzee.screen.game.scorer.DartsScorerGolf
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
        val scorer = DartsScorerGolf()
        scorer.init(null, null)
        panelMyScorecard.add(scorer, BorderLayout.CENTER)
    }

    override fun populateStats()
    {
        setOtherComponentVisibility(this, panelOther)

        populateStats(filteredGames, panelMyScorecard, null)
        if (includeOtherComparison())
        {
            populateStats(filteredGamesOther, panelOtherScorecard, Color.RED)
        }
    }

    private fun populateStats(filteredGames: List<GameWrapper>, panel: JPanel, color: Color?)
    {
        val hmHoleToBestDarts = HashMapList<Int, Dart>()
        val hmHoleToBestGameId = mutableMapOf<Int, Long>()

        val sortedGames = filteredGames.sortedBy { it.dtStart }
        for (game in sortedGames)
        {
            game.populateOptimalScorecardMaps(hmHoleToBestDarts, hmHoleToBestGameId)
        }

        val scorer = DartsScorerGolf()
        if (color != null)
        {
            scorer.setTableForeground(Color.RED)
        }

        scorer.showGameId = true
        scorer.init(null, null)

        for (i in 1..18)
        {
            val darts = hmHoleToBestDarts[i]
            if (darts != null)
            {
                val gameId = hmHoleToBestGameId[i]
                scorer.addDarts(darts, gameId!!)
            }
        }

        panel.removeAll()
        panel.add(scorer, BorderLayout.CENTER)
        panel.revalidate()
        panel.repaint()
    }

}
