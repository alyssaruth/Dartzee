package dartzee.dartzee.dart

import dartzee.`object`.DartboardSegment
import dartzee.core.bean.addUpdateListener
import dartzee.utils.InjectedThings
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

    val btnConfigure = JButton("Configure")
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
        segments.forEach { it.writeXml(rootElement, "Segment") }

        rootElement.setAttribute("Name", name)
    }

    override fun populate(rootElement: Element)
    {
        segments.addAll(DartboardSegment.readList(rootElement, "Segment"))
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
            segments = InjectedThings.dartzeeSegmentFactory.selectSegments(segments)
        }

        name = tfName.text

        //Propagate an action event to any other listeners
        btnConfigure.actionListeners.find { it != this }?.actionPerformed(e)
    }
}