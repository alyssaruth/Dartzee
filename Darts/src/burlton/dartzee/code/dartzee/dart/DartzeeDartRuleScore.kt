package burlton.dartzee.code.dartzee.dart

import burlton.core.code.util.getAttributeInt
import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.bean.SpinnerSingleSelector
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.FlowLayout
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartzeeDartRuleScore: AbstractDartzeeDartRuleConfigurable(), ChangeListener
{
    var score = 20

    val spinner = SpinnerSingleSelector()

    init
    {
        configPanel.layout = FlowLayout()
        configPanel.add(spinner)

        spinner.addChangeListener(this)
        spinner.value = score
    }

    override fun isValidSegment(segment: DartboardSegment): Boolean
    {
        return segment.score == score && !segment.isMiss()
    }

    override fun getRuleIdentifier() = "Score"
    override fun getDescription() = "$score"

    override fun writeXmlAttributes(doc: Document, rootElement: Element)
    {
        rootElement.setAttribute("Target", "$score")
    }

    override fun populate(rootElement: Element)
    {
        score = rootElement.getAttributeInt("Target")
        spinner.value = score
    }

    override fun stateChanged(e: ChangeEvent?)
    {
        score = spinner.value as Int
    }

    override fun randomise()
    {
        val list = mutableListOf(25)
        for (i in 1..20) { list.add(i) }

        score = list.random()
        spinner.value = score
    }
}