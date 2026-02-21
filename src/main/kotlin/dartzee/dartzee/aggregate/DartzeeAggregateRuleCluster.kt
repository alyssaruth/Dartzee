package dartzee.dartzee.aggregate

import dartzee.core.util.getAttributeInt
import dartzee.dartzee.IDartzeeRuleConfigurable
import dartzee.`object`.DartboardSegment
import dartzee.utils.getNumbersWithinN
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import kotlin.random.Random
import org.w3c.dom.Document
import org.w3c.dom.Element

class DartzeeAggregateRuleCluster : AbstractDartzeeAggregateRule(), IDartzeeRuleConfigurable {
    override val configPanel = JPanel()
    val spinner = JSpinner()

    init {
        configPanel.layout = FlowLayout()
        configPanel.add(spinner)

        spinner.model = SpinnerNumberModel(1, 1, 5, 1)
        spinner.value = 1
    }

    override fun isValidRound(segments: List<DartboardSegment>): Boolean {
        if (segments.any { it.isMiss() }) {
            return false
        }

        return segments.all { segment ->
            val others = (segments - segment).map { it.score }
            val valids = getNumbersWithinN(segment.score, spinner.value as Int)
            valids.containsAll(others)
        }
    }

    override fun getRuleIdentifier() = "DartsCluster"

    override fun toString() = "Darts spaced by at most "

    override fun getDescription() = "Darts spaced by at most ${spinner.value}"

    override fun writeXmlAttributes(doc: Document, rootElement: Element) {
        rootElement.setAttribute("Cluster", "${spinner.value}")
    }

    override fun populate(rootElement: Element) {
        spinner.value = rootElement.getAttributeInt("Cluster")
    }

    override fun randomise() {
        spinner.value = Random.nextInt(4) + 1
    }
}
