package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import burlton.dartzee.code.dartzee.dart.AbstractDartzeeDartRuleConfigurable
import burlton.dartzee.code.dartzee.getAllDartRules
import burlton.dartzee.code.dartzee.getAllTotalRules
import burlton.dartzee.code.dartzee.parseRule
import burlton.dartzee.code.dartzee.total.AbstractDartzeeRuleTotalSize
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCreationDialog
import burlton.desktopcore.code.bean.findByConcreteClass
import burlton.desktopcore.code.bean.selectedItemTyped
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.addActionListenerToAllChildren
import burlton.desktopcore.code.util.addChangeListenerToAllChildren
import burlton.desktopcore.code.util.enableChildren
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

class DartzeeRuleSelector(desc: String, val total: Boolean = false, val optional: Boolean = false): JPanel(), ActionListener
{
    val cbDesc = JCheckBox(desc)
    val lblDesc = JLabel(desc)
    val comboBoxRuleType = JComboBox<AbstractDartzeeRule>()
    var listener: DartzeeRuleCreationDialog? = null

    init
    {
        layout = FlowLayout()

        populateComboBox()

        comboBoxRuleType.addActionListener(this)
        cbDesc.addActionListener(this)

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

        if (optional)
        {
            cbDesc.isSelected = true
        }

        item.populate(ruleStr)
        updateComponents()
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
        cbDesc.isEnabled = true
    }

    override fun actionPerformed(e: ActionEvent?)
    {
        updateComponents()
    }

    fun addActionListener(listener: DartzeeRuleCreationDialog)
    {
        this.listener = listener
        addActionListenerToAllChildren(listener)
        addChangeListenerToAllChildren(listener)
    }

    private fun updateComponents()
    {
        val rule = getSelection()

        removeAll()

        if (optional) add(cbDesc) else add(lblDesc)
        add(comboBoxRuleType)

        //TODO - Figure out a nicer way
        if (rule is AbstractDartzeeDartRuleConfigurable)
        {
            add(rule.configPanel)
        }

        if (rule is AbstractDartzeeRuleTotalSize)
        {
            add(rule.configPanel)
        }

        listener?.let {
            addActionListenerToAllChildren(it)
            addChangeListenerToAllChildren(it)
        }

        this.isEnabled = !optional || cbDesc.isSelected

        revalidate()
    }
}