package dartzee.screen.stats.player.golf

import dartzee.`object`.Dart
import dartzee.core.util.getSortedValues
import dartzee.db.ParticipantEntity
import dartzee.game.state.GolfPlayerState
import dartzee.game.state.SingleParticipant
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
        val hmHoleToBestDarts = mutableMapOf<Int, List<Dart>>()
        val hmHoleToBestGameId = mutableMapOf<Int, Long>()

        val sortedGames = filteredGames.sortedBy { it.dtStart }
        for (game in sortedGames)
        {
            game.populateOptimalScorecardMaps(hmHoleToBestDarts, hmHoleToBestGameId)
        }

        val fudgedParticipant = SingleParticipant(ParticipantEntity())
        val scorer = DartsScorerGolf(fudgedParticipant, showGameId = true)
        if (color != null)
        {
            scorer.setTableForeground(Color.RED)
        }

        val state = GolfPlayerState(fudgedParticipant, hmHoleToBestDarts.values.toMutableList())
        scorer.stateChanged(state)

        scorer.addGameIds(hmHoleToBestGameId.getSortedValues())

        panel.removeAll()
        panel.add(scorer.getTableOnly(), BorderLayout.CENTER)
        panel.revalidate()
        panel.repaint()
    }

}
