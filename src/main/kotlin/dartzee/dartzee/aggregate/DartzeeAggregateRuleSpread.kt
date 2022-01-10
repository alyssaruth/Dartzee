package dartzee.dartzee.aggregate

import dartzee.`object`.DartboardSegment
import dartzee.core.util.getAttributeInt
import dartzee.dartzee.IDartzeeRuleConfigurable
import dartzee.utils.getNumbersWithinN
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import kotlin.random.Random

class DartzeeAggregateRuleSpread: AbstractDartzeeAggregateRule(), IDartzeeRuleConfigurable
{
    override val configPanel = JPanel()
    val spinner = JSpinner()

    init
    {
        configPanel.layout = FlowLayout()
        configPanel.add(spinner)

        spinner.model = SpinnerNumberModel(1, 1, 5, 1)
        spinner.value = 1
    }

    override fun isValidRound(segments: List<DartboardSegment>): Boolean
    {
        if (segments.any { it.isMiss() || it.score == 25 }) {
            return false
        }

        val valids = (1..20).toMutableSet()
        segments.forEach { segment ->
            if (!valids.contains(segment.score)) {
                return false
            }

            valids.removeAll(getNumbersWithinN(segment.score, spinner.value as Int))
        }

        return true
    }

    override fun getRuleIdentifier() = "DartsSpread"
    override fun toString() = "Darts spaced by at least "
    override fun getDescription() = "Darts spaced by at least ${spinner.value}"

    override fun writeXmlAttributes(doc: Document, rootElement: Element)
    {
        rootElement.setAttribute("Spread", "${spinner.value}")
    }

    override fun populate(rootElement: Element)
    {
        spinner.value = rootElement.getAttributeInt("Spread")
    }

    override fun randomise()
    {
        spinner.value = Random.nextInt(4) + 1
    }
}