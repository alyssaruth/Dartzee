package burlton.dartzee.code.dartzee

import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.bean.SpinnerSingleSelector
import org.w3c.dom.Element
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartzeeDartRuleNumber: AbstractDartzeeDartRule(), ChangeListener
{
    private var score = -1

    private val configPanel = JPanel()
    private val spinner = SpinnerSingleSelector()

    init
    {
        configPanel.layout = FlowLayout()
        configPanel.add(spinner)

        spinner.addChangeListener(this)
        spinner.value = 20
    }

    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segment.score == score
    }

    override fun getRuleIdentifier() = "Score"

    override fun writeXmlAttributes(rootElement: Element)
    {
        rootElement.setAttribute("Target", "$score")
    }

    override fun populate(rootElement: Element)
    {
        score = XmlUtil.getAttributeInt(rootElement, "Target")
    }

    override fun getConfigPanel() = configPanel
    override fun stateChanged(e: ChangeEvent?)
    {
        score = spinner.value as Int
    }
}