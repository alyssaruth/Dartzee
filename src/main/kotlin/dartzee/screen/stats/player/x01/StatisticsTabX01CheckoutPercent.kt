package dartzee.screen.stats.player.x01

import dartzee.core.bean.ScrollTable
import dartzee.core.obj.HashMapList
import dartzee.core.util.MathsUtil
import dartzee.core.util.TableUtil.DefaultModel
import dartzee.`object`.Dart
import dartzee.screen.stats.player.AbstractStatisticsTab
import dartzee.stats.GameWrapper
import dartzee.utils.getCheckoutScores
import dartzee.utils.isCheckoutDart
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel

/**
 * Checkout percentages for X01
 */
class StatisticsTabX01CheckoutPercent : AbstractStatisticsTab()
{
    private val panelMine = JPanel()
    private val tableMine = ScrollTable()
    private val panelOther = JPanel()
    private val tableOther = ScrollTable()

    init
    {
        layout = GridLayout(0, 2, 0, 0)

        add(panelMine)
        panelMine.layout = BorderLayout(0, 0)
        panelMine.add(tableMine, BorderLayout.CENTER)
        add(panelOther)
        panelOther.layout = BorderLayout(0, 0)
        panelOther.add(tableOther, BorderLayout.CENTER)
        tableOther.tableForeground = Color.RED
    }

    override fun populateStats()
    {
        setOtherComponentVisibility(this, panelOther)

        populateTable(tableMine, filteredGames)
        if (includeOtherComparison())
        {
            populateTable(tableOther, filteredGamesOther)
        }

    }

    private fun populateTable(table: ScrollTable, games: List<GameWrapper>)
    {
        val hmDoubleToDartsThrown = HashMapList<Int, Dart>()
        for (g in games)
        {
            addDartsToHashMap(g, hmDoubleToDartsThrown)
        }

        val model = DefaultModel()
        model.addColumn("Double")
        model.addColumn("Opportunities")
        model.addColumn("Hits")
        model.addColumn("Checkout %")

        var totalOpportunities = 0
        var totalHits = 0

        getCheckoutScores().forEach { checkout ->
            val darts = hmDoubleToDartsThrown[checkout] ?: mutableListOf()

            val opportunities = darts.size
            val hits = darts.filter { drt -> drt.isDouble() && drt.getTotal() == checkout }.size

            val row = arrayOf(checkout / 2, opportunities, hits, MathsUtil.getPercentage(hits, opportunities.toDouble()))
            model.addRow(row)

            totalOpportunities += opportunities
            totalHits += hits
        }

        table.model = model

        val totalsRow = arrayOf("", totalOpportunities, totalHits, MathsUtil.getPercentage(totalHits, totalOpportunities.toDouble()))
        table.addFooterRow(totalsRow)

        table.sortBy(0, false)
    }

    private fun addDartsToHashMap(g: GameWrapper, hmDoubleToDartsThrown: HashMapList<Int, Dart>)
    {
        val darts = g.getAllDarts()
        for (drt in darts)
        {
            if (isCheckoutDart(drt))
            {
                val startingScore = drt.startingScore
                hmDoubleToDartsThrown.putInList(startingScore, drt)
            }
        }
    }
}