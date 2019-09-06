package burlton.dartzee.code.bean

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import burlton.dartzee.code.dartzee.IDartzeeRuleConfigurable
import burlton.dartzee.code.screen.dartzee.DartzeeRuleCreationDialog
import burlton.desktopcore.code.bean.findByConcreteClass
import burlton.desktopcore.code.bean.selectedItemTyped
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.addActionListenerToAllChildren
import burlton.desktopcore.code.util.addChangeListenerToAllChildren
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

abstract class AbstractDartzeeRuleSelector<BaseRuleType: AbstractDartzeeRule>(val desc: String): JPanel(), ActionListener
{
    val lblDesc = JLabel(desc)
    val cbDesc = JCheckBox(desc)

    val comboBoxRuleType = JComboBox<BaseRuleType>()
    var listener: DartzeeRuleCreationDialog? = null

    init
    {
        layout = FlowLayout()

        populateComboBox()

        comboBoxRuleType.addActionListener(this)
        cbDesc.addActionListener(this)

        updateComponents()
    }

    abstract fun getRules(): List<BaseRuleType>
    open fun isOptional() = false

    private fun populateComboBox()
    {
        val rules = getRules()

        val model = DefaultComboBoxModel<BaseRuleType>()

        rules.forEach{
            model.addElement(it)
        }

        comboBoxRuleType.model = model
    }

    fun getSelection() = comboBoxRuleType.selectedItemTyped()

    open fun populate(rule: BaseRuleType)
    {
        val item = comboBoxRuleType.findByConcreteClass(rule.javaClass)!!
        item.populate(rule.toDbString())

        comboBoxRuleType.selectedItem = item
        cbDesc.isSelected = true

        updateComponents()
    }

    fun valid(): Boolean
    {
        val errorStr = getSelection().validate()
        if (!errorStr.isEmpty())
        {
            DialogUtil.showError("$desc: $errorStr")
            return false
        }

        return true
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

    open fun shouldBeEnabled() = true

    private fun updateComponents()
    {
        val rule = getSelection()

        removeAll()

        if (isOptional()) add(cbDesc) else add(lblDesc)

        add(comboBoxRuleType)

        if (rule is IDartzeeRuleConfigurable)
        {
            add(rule.configPanel)
        }

        listener?.let {
            addActionListenerToAllChildren(it)
            addChangeListenerToAllChildren(it)
        }

        this.isEnabled = shouldBeEnabled()

        revalidate()
    }
}