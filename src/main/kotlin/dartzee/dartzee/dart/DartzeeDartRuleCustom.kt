package dartzee.dartzee.dart

import dartzee.core.bean.addUpdateListener
import dartzee.`object`.DartboardSegment
import dartzee.utils.InjectedThings
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JTextField
import org.w3c.dom.Document
import org.w3c.dom.Element

class DartzeeDartRuleCustom : AbstractDartzeeDartRuleConfigurable(), ActionListener {
    val segments = mutableSetOf<DartboardSegment>()
    var name = ""

    val btnConfigure = JButton("Configure")
    val tfName = JTextField()

    init {
        configPanel.layout = FlowLayout()
        configPanel.add(btnConfigure)
        configPanel.add(tfName)

        tfName.columns = 15
        tfName.addActionListener(this)
        tfName.addUpdateListener(this)
        btnConfigure.addActionListener(this)
    }

    override fun isValidSegment(segment: DartboardSegment) =
        segments.any { it.score == segment.score && it.type == segment.type }

    override fun getRuleIdentifier() = "Custom"

    override fun getDescription() = name.ifEmpty { "Custom" }

    override fun writeXmlAttributes(doc: Document, rootElement: Element) {
        segments.forEach { it.writeXml(rootElement, "Segment") }

        rootElement.setAttribute("Name", name)
    }

    override fun populate(rootElement: Element) {
        segments.addAll(DartboardSegment.readList(rootElement, "Segment"))
        name = rootElement.getAttribute("Name")
        tfName.text = name
    }

    override fun validate(): String {
        if (segments.isEmpty()) {
            return "You must select at least one segment."
        }

        return ""
    }

    override fun actionPerformed(e: ActionEvent?) {
        if (e?.source == btnConfigure) {
            val updatedSelection =
                InjectedThings.dartzeeSegmentFactory.selectSegments(segments.toSet())
            segments.clear()
            segments.addAll(updatedSelection)
        }

        name = tfName.text

        // Propagate an action event to any other listeners
        btnConfigure.actionListeners.find { it != this }?.actionPerformed(e)
    }
}
