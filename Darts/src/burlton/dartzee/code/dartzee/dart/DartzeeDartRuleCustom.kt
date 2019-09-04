package burlton.dartzee.code.dartzee.dart

import burlton.dartzee.code.`object`.DartboardSegment
import burlton.dartzee.code.screen.DartboardSegmentSelectDialog
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCreationDialog
import burlton.desktopcore.code.bean.addUpdateListener
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JTextField

class DartzeeDartRuleCustom: AbstractDartzeeDartRuleConfigurable(), ActionListener
{
    var segments = hashSetOf<DartboardSegment>()
    var name = ""

    private val btnConfigure = JButton("Configure")
    val tfName = JTextField()

    init
    {
        configPanel.layout = FlowLayout()
        configPanel.add(btnConfigure)
        configPanel.add(tfName)

        tfName.columns = 15
        tfName.addActionListener(this)
        tfName.addUpdateListener(this)
        btnConfigure.addActionListener(this)
    }

    override fun isValidSegment(segment: DartboardSegment): Boolean
    {
        return segments.find{it.score == segment.score && it.type == segment.type} != null
    }

    override fun getRuleIdentifier() = "Custom"

    override fun getDescription() = if (name.isEmpty()) "Custom" else name

    override fun writeXmlAttributes(doc: Document, rootElement: Element)
    {
        segments.forEach{
            val element = doc.createElement("Segment")
            element.setAttribute("Value", it.scoreAndType)

            rootElement.appendChild(element)
        }

        rootElement.setAttribute("Name", name)
    }

    override fun populate(rootElement: Element)
    {
        val list = rootElement.getElementsByTagName("Segment")
        for (i in 0 until list.length)
        {
            val node = list.item(i) as Element
            val segment = DartboardSegment(node.getAttribute("Value"))

            segments.add(segment)
        }

        name = rootElement.getAttribute("Name")
        tfName.text = name
    }

    override fun validate(): String
    {
        if (segments.isEmpty())
        {
            return "You must select at least one segment."
        }

        return ""
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        if (e?.source == btnConfigure)
        {
            val dlg = DartboardSegmentSelectDialog(segments)
            dlg.isVisible = true
            segments = dlg.getSelection()
        }

        name = tfName.text

        //Need to fire off something to tell the other screen to update. Shit.
        btnConfigure.actionListeners.find { it is DartzeeRuleCreationDialog }?.actionPerformed(e)
    }

}