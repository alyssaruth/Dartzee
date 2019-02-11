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
    var segments = mutableListOf<DartboardSegmentKt>()

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
            element.nodeValue = it.scoreAndType

            rootElement.appendChild(element)
        }
    }

    override fun populate(rootElement: Element)
    {
        for (i in 0 until rootElement.childNodes.length)
        {
            val node = rootElement.childNodes.item(i)
            val segment = DartboardSegmentKt(node.nodeValue)

            segments.add(segment)
        }
    }

    override fun getConfigPanel() = configPanel
    override fun actionPerformed(e: ActionEvent?)
    {
        val dlg = DartboardSegmentSelectDialog()
        dlg.isVisible = true
    }

}