package burlton.dartzee.code.dartzee

import burlton.dartzee.code.`object`.DartboardSegmentKt
import burlton.dartzee.code.screen.DartboardSegmentSelectDialog
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JPanel

class DartzeeDartRuleCustom: AbstractDartzeeDartRule(), ActionListener
{
    var segments = hashSetOf<DartboardSegmentKt>()

    private val configPanel = JPanel()
    private val btnConfigure = JButton("Configure")

    init
    {
        configPanel.layout = FlowLayout()
        configPanel.add(btnConfigure)

        btnConfigure.addActionListener(this)
    }

    override fun isValidSegment(segment: DartboardSegmentKt): Boolean
    {
        return segments.find{it.score == segment.score && it.type == segment.type} != null
    }

    override fun getRuleIdentifier() = "Custom"

    override fun writeXmlAttributes(doc: Document, rootElement: Element)
    {
        segments.forEach{
            val element = doc.createElement("Segment")
            element.setAttribute("Value", it.scoreAndType)

            rootElement.appendChild(element)
        }
    }

    override fun populate(rootElement: Element)
    {
        val list = rootElement.getElementsByTagName("Segment")
        for (i in 0 until list.length)
        {
            val node = list.item(i) as Element
            val segment = DartboardSegmentKt(node.getAttribute("Value"))

            segments.add(segment)
        }
    }

    override fun validate(): String
    {
        if (segments.isEmpty())
        {
            return "You must select at least one segment."
        }

        return ""
    }

    override fun getConfigPanel() = configPanel
    override fun actionPerformed(e: ActionEvent?)
    {
        val dlg = DartboardSegmentSelectDialog(segments)
        dlg.isVisible = true

        segments = dlg.getSelection()
    }

}