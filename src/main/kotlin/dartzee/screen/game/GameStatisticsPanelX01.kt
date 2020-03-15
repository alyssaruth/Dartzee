package dartzee.screen.game

import dartzee.`object`.Dart
import dartzee.core.bean.NumberField
import dartzee.core.util.MathsUtil
import dartzee.core.util.maxOrZero
import dartzee.core.util.minOrZero
import dartzee.utils.calculateThreeDartAverage
import dartzee.utils.getScoringDarts
import dartzee.utils.isCheckoutDart
import dartzee.utils.sumScore
import java.awt.BorderLayout
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
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

        addRow(getScoreRow("Highest Score") { it.maxOrZero() })
        addRow(getThreeDartAvgsRow())
        addRow(getScoreRow("Lowest Score") { it.minOrZero() })
        addRow(getMultiplePercent("Miss %", 0))
        addRow(getMultiplePercent("Treble %", 3))

        addRow(getScoresBetween(180, 181, "180"))
        addRow(getScoresBetween(140, 180, "140 - 179"))
        addRow(getScoresBetween(100, 140, "100 - 139"))
        addRow(getScoresBetween(80, 100, "80 - 99"))
        addRow(getScoresBetween(60, 80, "60 - 79"))
        addRow(getScoresBetween(40, 60, "40 - 59"))
        addRow(getScoresBetween(20, 40, "20 - 39"))
        addRow(getScoresBetween(0, 20, "0 - 19"))

        addTopDartsRows()

        addRow(getCheckoutPercentRow())

        table.setColumnWidths("120")
    }

    private fun addTopDartsRows()
    {
        val topDarts = arrayOfNulls<Any>(getRowWidth())
        val secondDarts = arrayOfNulls<Any>(getRowWidth())
        val thirdDarts = arrayOfNulls<Any>(getRowWidth())
        val fourthDarts = arrayOfNulls<Any>(getRowWidth())
        val fifthDarts = arrayOfNulls<Any>(getRowWidth())
        val remainingDarts = arrayOfNulls<Any>(getRowWidth())

        topDarts[0] = "Top Darts"
        remainingDarts[0] = "Remainder"
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val darts = getScoringDarts(playerName)

            val hmHitScoreToDarts = darts.groupBy{it.getHitScore()}
            val sortedEntries = hmHitScoreToDarts.entries
                                                 .sortedWith(compareByDescending<Map.Entry<Int, List<Dart>>> {it.value.size}
                                                 .thenByDescending{it.key})
                                                 .toMutableList()

            parseTopDartEntry(sortedEntries, topDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, secondDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, thirdDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, fourthDarts, i, darts.size)
            parseTopDartEntry(sortedEntries, fifthDarts, i, darts.size)

            //Deal with the remainder
            val remainder = sortedEntries.map { it.value.size }.sum().toDouble()
            val percent = MathsUtil.round(100*remainder / darts.size, 1)
            remainingDarts[i+1] = "$percent%"
        }

        addRow(topDarts)
        addRow(secondDarts)
        addRow(thirdDarts)
        addRow(fourthDarts)
        addRow(fifthDarts)
        addRow(remainingDarts)
    }
    private fun parseTopDartEntry(sortedEntries: MutableList<Map.Entry<Int, List<Dart>>>, row: Array<Any?>,
                                  i: Int, totalDarts: Int)
    {
        if (sortedEntries.isEmpty())
        {
            row[i+1] = "N/A [0.0%]"
        }
        else
        {
            val entry = sortedEntries.removeAt(0)
            val percent = MathsUtil.round((100*entry.value.size.toDouble()) / totalDarts, 0).toInt()

            row[i+1] = "${entry.key} [$percent%]"
        }
    }


    private fun getThreeDartAvgsRow(): Array<Any?>
    {
        val threeDartAvgs = arrayOfNulls<Any>(getRowWidth())
        threeDartAvgs[0] = "3-dart avg"
        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val rounds = hmPlayerToDarts[playerName]
            val darts = rounds!!.flatten()

            var avg = calculateThreeDartAverage(darts, nfSetupThreshold.getNumber())
            if (avg < 0)
            {
                threeDartAvgs[i + 1] = "N/A"
            }
            else
            {
                val p1 = (100 * avg).toInt()
                avg = p1.toDouble() / 100

                threeDartAvgs[i + 1] = avg
            }


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
            val actualFinishes = potentialFinishers.filter { d -> d.isDouble() && d.getTotal() == d.startingScore }

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

    private fun getScoreRow(desc: String, f: (i: List<Int>) -> Int): Array<Any?>
    {
        val row = arrayOfNulls<Any>(getRowWidth())
        row[0] = desc

        for (i in playerNamesOrdered.indices)
        {
            val playerName = playerNamesOrdered[i]
            val rounds = getScoringRounds(playerName)

            if (rounds.isNotEmpty())
            {
                val roundsAsTotal = rounds.map { rnd -> sumScore(rnd) }
                row[i + 1] = f.invoke(roundsAsTotal)
            }
            else
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

    private fun getScoringRounds(playerName: String): List<List<Dart>>
    {
        val rounds = hmPlayerToDarts[playerName]
        rounds ?: return mutableListOf()

        return rounds.filter { it.last().startingScore > nfSetupThreshold.getNumber() }.toList()
    }

    private fun getScoringDarts(playerName: String): MutableList<Dart>
    {
        val darts = getFlattenedDarts(playerName)
        return getScoringDarts(darts, nfSetupThreshold.getNumber())
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

    override fun getStartOfSectionRows(): MutableList<String>
    {
        return mutableListOf("180", "Top Darts", "Checkout %", "Best Game")
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
