package burlton.dartzee.code.screen.game

import burlton.core.code.util.MathsUtil
import burlton.core.code.util.flattenBatches
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.utils.calculateThreeDartAverage
import burlton.dartzee.code.utils.getScoringDarts
import burlton.dartzee.code.utils.isCheckoutDart
import burlton.dartzee.code.utils.sumScore
import burlton.desktopcore.code.bean.NumberField
import java.awt.BorderLayout
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.util.stream.IntStream
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Shows running stats for X01 games - three-dart average, checkout % etc.
 */
open class GameStatisticsPanelX01 : GameStatisticsPanel(), PropertyChangeListener
{
    private val panel = JPanel()
    private val lblSetupThreshold = JLabel("Setup Threshold")
    private val nfSetupThreshold = NumberField()

    init
    {
        add(panel, BorderLayout.NORTH)
        panel.add(lblSetupThreshold)

        panel.add(nfSetupThreshold)
        nfSetupThreshold.columns = 10

        nfSetupThreshold.value = 100
        nfSetupThreshold.addPropertyChangeListener(this)
    }

    override fun addRowsToTable()
    {
        nfSetupThreshold.setMinimum(62)
        nfSetupThreshold.setMaximum(Integer.parseInt(gameParams) - 1)

        addRow(getScoreRow({i -> i.max().asInt}, "Highest Score"))
        addRow(getThreeDartAvgsRow())
        addRow(getScoreRow({i -> i.min().asInt}, "Lowest Score"))
        addRow(getMultiplePercent("Miss %", 0))
        addRow(getMultiplePercent("Treble %", 3))

        addRow(arrayOfNulls(getRowWidth()))

        addRow(getScoresBetween(180, 181, "180"))
        addRow(getScoresBetween(140, 180, "140 - 179"))
        addRow(getScoresBetween(100, 140, "100 - 139"))
        addRow(getScoresBetween(80, 100, "80 - 99"))
        addRow(getScoresBetween(60, 80, "60 - 79"))
        addRow(getScoresBetween(40, 60, "40 - 59"))
        addRow(getScoresBetween(20, 40, "20 - 39"))
        addRow(getScoresBetween(0, 20, "0 - 19"))

        addRow(arrayOfNulls(getRowWidth()))

        addRow(getCheckoutPercentRow())

        table.setColumnWidths("120")
    }

    private fun getThreeDartAvgsRow(): Array<Any?>
    {
        val threeDartAvgs = arrayOfNulls<Any>(getRowWidth())
        threeDartAvgs[0] = "3-dart avg"
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val rounds = hmPlayerToDarts[playerName]
            val darts = rounds!!.flattenBatches()

            var avg = calculateThreeDartAverage(darts, nfSetupThreshold.number)
            val p1 = (100 * avg).toInt()
            avg = p1.toDouble() / 100

            threeDartAvgs[i + 1] = avg
        }

        return threeDartAvgs
    }

    private fun getCheckoutPercentRow(): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = "Checkout %"

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val darts = getFlattenedDarts(playerName)

            val potentialFinishers = darts.filter { d -> isCheckoutDart(d) }
            val actualFinishes = potentialFinishers.filter { d -> d.isDouble && d.total == d.startingScore }

            if (actualFinishes.isEmpty())
            {
                row[i + 1] = "N/A"
            }
            else
            {
                val p1 = 10000 * actualFinishes.size / potentialFinishers.size
                val percent = p1.toDouble() / 100

                row[i + 1] = percent
            }
        }

        return row
    }

    private fun getScoresBetween(min: Int, max: Int, desc: String): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = desc

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val rounds = getScoringRounds(playerName)

            val bigRounds = rounds.filter { r -> sumScore(r) in min until max }

            row[i + 1] = bigRounds.size
        }

        return row
    }

    private fun getScoreRow(f: (i: IntStream) -> Int, desc: String): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = desc

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val rounds = getScoringRounds(playerName)

            if (!rounds.isEmpty())
            {
                val roundsAsTotal = rounds.stream().mapToInt { rnd -> sumScore(rnd) }
                row[i + 1] = f.invoke(roundsAsTotal)
            } else
            {
                row[i + 1] = "N/A"
            }
        }

        return row
    }

    private fun getMultiplePercent(desc: String, multiplier: Int): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = desc

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val scoringDarts = getScoringDarts(playerName)
            val hits = scoringDarts.filter { d -> d.multiplier == multiplier }

            var percent = 100 * hits.size.toDouble() / scoringDarts.size
            percent = MathsUtil.round(percent, 2)

            row[i + 1] = percent
        }

        return row
    }

    private fun getScoringRounds(playerName: String): MutableList<MutableList<Dart>>
    {
        val rounds = hmPlayerToDarts[playerName]
        rounds ?: return mutableListOf()

        return rounds.filter{r -> r.last().startingScore > nfSetupThreshold.number}.toMutableList()
    }

    private fun getScoringDarts(playerName: String): MutableList<Dart>
    {
        val darts = getFlattenedDarts(playerName)
        return getScoringDarts(darts, nfSetupThreshold.number)
    }

    override fun getRankedRowsHighestWins(): MutableList<String>
    {
        return mutableListOf("Highest Score", "3-dart avg", "Lowest Score", "Treble %", "Checkout %")
    }

    override fun getRankedRowsLowestWins(): MutableList<String>
    {
        return mutableListOf("Miss %")
    }

    override fun getHistogramRows(): MutableList<String>
    {
        return mutableListOf("180", "140 - 179", "100 - 139", "80 - 99", "60 - 79", "40 - 59", "20 - 39", "0 - 19")
    }

    override fun propertyChange(arg0: PropertyChangeEvent)
    {
        val propertyName = arg0.propertyName
        if (propertyName == "value")
        {
            buildTableModel()
            repaint()
        }
    }
}
