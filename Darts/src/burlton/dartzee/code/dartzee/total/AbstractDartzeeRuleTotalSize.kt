package burlton.dartzee.code.dartzee.total

import burlton.core.code.util.XmlUtil
import burlton.dartzee.code.dartzee.IDartzeeRuleConfigurable
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

abstract class AbstractDartzeeRuleTotalSize: AbstractDartzeeTotalRule(), ChangeListener, IDartzeeRuleConfigurable
{
    var target = 20

    override val configPanel = JPanel()
    val spinner = JSpinner()

    init
    {
        configPanel.layout = FlowLayout()
        configPanel.add(spinner)

        spinner.model = SpinnerNumberModel(target, 3, 180, 1)

        spinner.addChangeListener(this)
        spinner.value = target
    }

    override fun writeXmlAttributes(doc: Document, rootElement: Element)
    {
        rootElement.setAttribute("Target", "$target")
    }

    override fun populate(rootElement: Element)
    {
        target = XmlUtil.getAttributeInt(rootElement, "Target")
        spinner.value = target
    }

    override fun stateChanged(e: ChangeEvent?)
    {
        target = spinner.value as Int
    }
}