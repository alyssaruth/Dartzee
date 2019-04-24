package burlton.dartzee.code.screen.stats.player.x01

import burlton.core.code.obj.HandyArrayList
import burlton.core.code.obj.HashMapList
import burlton.core.code.util.MathsUtil
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.screen.stats.player.AbstractStatisticsTab
import burlton.dartzee.code.stats.GameWrapper
import burlton.dartzee.code.utils.getCheckoutScores
import burlton.dartzee.code.utils.isCheckoutDart
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil.DefaultModel
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

    private fun populateTable(table: ScrollTable, games: HandyArrayList<GameWrapper>)
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

        getCheckoutScores().forEach{
            val darts = hmDoubleToDartsThrown[it] ?: mutableListOf()

            val opportunities = darts.size
            val hits = darts.filter { drt -> drt.isDouble() && drt.getTotal() == it }.size

            val row = arrayOf(it / 2, opportunities, hits, MathsUtil.getPercentage(hits, opportunities.toDouble()))
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