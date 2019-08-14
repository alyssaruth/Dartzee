package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.*
import burlton.desktopcore.code.bean.findByConcreteClass
import burlton.desktopcore.code.bean.selectedItemTyped
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.addActionListenerToAllChildren
import burlton.desktopcore.code.util.enableChildren
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class DartzeeRuleSelector(desc: String, val total: Boolean = false): JPanel(), ActionListener
{
    val lblDesc = JLabel(desc)
    val comboBoxRuleType = JComboBox<AbstractDartzeeRule>()
    var listener: ActionListener? = null

    init
    {
        layout = FlowLayout()

        populateComboBox()

        comboBoxRuleType.addActionListener(this)

        updateComponents()
    }

    private fun populateComboBox()
    {
        val rules = getRules()

        val model = DefaultComboBoxModel<AbstractDartzeeRule>()

        rules.forEach{
            model.addElement(it)
        }

        comboBoxRuleType.model = model
    }
    private fun getRules() = if (total) getAllTotalRules() else getAllDartRules()

    fun getSelection() = comboBoxRuleType.selectedItemTyped()

    fun populate(ruleStr: String)
    {
        val rule = parseRule(ruleStr, getRules())!!

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

    override fun setEnabled(enabled: Boolean)
    {
        super.setEnabled(enabled)

        enableChildren(enabled)
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        updateComponents()
    }

    fun addActionListener(listener: ActionListener)
    {
        this.listener = listener
        addActionListenerToAllChildren(listener)
    }

    private fun updateComponents()
    {
        val rule = getSelection()

        removeAll()
        add(lblDesc)
        add(comboBoxRuleType)

        if (rule is AbstractDartzeeRuleConfigurable)
        {
            add(rule.configPanel)
        }

        listener?.let { this.addActionListenerToAllChildren(it) }

        revalidate()
    }
}