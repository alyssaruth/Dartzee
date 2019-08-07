package burlton.dartzee.code.dartzee

import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.bean.SpinnerSingleSelector
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.FlowLayout
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartzeeDartRuleScore: AbstractDartzeeDartRuleConfigurable(), ChangeListener
{
    var score = 20

    private val spinner = SpinnerSingleSelector()

    init
    {
        configPanel.layout = FlowLayout()
        configPanel.add(spinner)

        spinner.addChangeListener(this)
        spinner.value = score
    }

    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.score == score && !segment.isMiss()
    }

    override fun getRuleIdentifier() = "Score"

    override fun writeXmlAttributes(doc: Document, rootElement: Element)
    {
        rootElement.setAttribute("Target", "$score")
    }

    override fun populate(rootElement: Element)
    {
        score = XmlUtil.getAttributeInt(rootElement, "Target")
    }

    override fun stateChanged(e: ChangeEvent?)
    {
        score = spinner.value as Int
    }
}