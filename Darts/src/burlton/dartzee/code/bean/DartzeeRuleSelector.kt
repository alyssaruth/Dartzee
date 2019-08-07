package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.AbstractDartzeeDartRule
import burlton.dartzee.code.dartzee.AbstractDartzeeDartRuleConfigurable
import burlton.dartzee.code.dartzee.getAllDartRules
import burlton.dartzee.code.dartzee.parseDartzeeRule
import burlton.desktopcore.code.bean.findByConcreteClass
import burlton.desktopcore.code.bean.selectedItemTyped
import burlton.desktopcore.code.util.DialogUtil
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class DartzeeRuleSelector(desc: String): JPanel(), ActionListener
{
    val lblDesc = JLabel(desc)
    val comboBoxRuleType = JComboBox<AbstractDartzeeDartRule>()

    init
    {
        layout = FlowLayout()

        populateComboBox()

        comboBoxRuleType.addActionListener(this)

        add(lblDesc)
        add(comboBoxRuleType)
    }

    private fun populateComboBox()
    {
        val rules = getAllDartRules()

        val model = DefaultComboBoxModel<AbstractDartzeeDartRule>()

        rules.forEach{
            model.addElement(it)
        }

        comboBoxRuleType.model = model
    }

    fun getSelection() = comboBoxRuleType.selectedItemTyped()

    fun populate(ruleStr: String)
    {
        val rule = parseDartzeeRule(ruleStr)!!

        val item = comboBoxRuleType.findByConcreteClass(rule.javaClass)!!
        comboBoxRuleType.selectedItem = item

        item.populate(ruleStr)
    }

    fun valid(): Boolean
    {
        val errorStr = getSelection().validate()
        if (!errorStr.isEmpty())
        {
            DialogUtil.showError("${lblDesc.text}: $errorStr")
            return false
        }

        return true
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        val rule = getSelection()

        removeAll()
        add(lblDesc)
        add(comboBoxRuleType)

        if (rule is AbstractDartzeeDartRuleConfigurable)
        {
            add(rule.configPanel)
        }

        revalidate()
    }
}