package dartzee.dartzee.aggregate

import dartzee.core.util.getAttributeInt
import dartzee.dartzee.IDartzeeRuleConfigurable
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import kotlin.random.Random
import org.w3c.dom.Document
import org.w3c.dom.Element

abstract class AbstractDartzeeRuleTotalSize :
    AbstractDartzeeTotalRule(), ChangeListener, IDartzeeRuleConfigurable {
    var target = 20

    override val configPanel = JPanel()
    val spinner = JSpinner()

    init {
        configPanel.layout = FlowLayout()
        configPanel.add(spinner)

        spinner.model = SpinnerNumberModel(target, 3, 180, 1)

        spinner.addChangeListener(this)
        spinner.value = target
    }

    override fun writeXmlAttributes(doc: Document, rootElement: Element) {
        rootElement.setAttribute("Target", "$target")
    }

    override fun populate(rootElement: Element) {
        target = rootElement.getAttributeInt("Target")
        spinner.value = target
    }

    override fun stateChanged(e: ChangeEvent?) {
        target = spinner.value as Int
    }

    override fun randomise() {
        target = Random.nextInt(178) + 3
        spinner.value = target
    }
}
