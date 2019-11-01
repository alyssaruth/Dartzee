package burlton.dartzee.code.bean

import burlton.dartzee.code.db.DartzeeTemplateEntity
import burlton.desktopcore.code.bean.ComboBoxItem
import burlton.desktopcore.code.bean.items
import burlton.desktopcore.code.bean.selectedItemTyped
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.JComboBox
import javax.swing.JPanel

class GameParamFilterPanelDartzee: GameParamFilterPanel()
{
    private val panel = JPanel()
    val comboBox = JComboBox<ComboBoxItem<DartzeeTemplateEntity?>>()

    init
    {
        add(panel, BorderLayout.CENTER)
        panel.add(comboBox)

        populateComboBox()
    }

    private fun populateComboBox()
    {
        val templates = DartzeeTemplateEntity().retrieveEntities()

        val divider = ComboBoxItem<DartzeeTemplateEntity?>(null, "-----", false)

        comboBox.addItem(ComboBoxItem(null, "Custom"))
        comboBox.addItem(divider)
        templates.forEach { comboBox.addItem(ComboBoxItem(it, it.name)) }

        if (templates.isEmpty())
        {
            val noTemplates = ComboBoxItem<DartzeeTemplateEntity?>(null, "No templates configured", false)
            comboBox.addItem(noTemplates)
        }
    }

    override fun setGameParams(gameParams: String)
    {
        val item = comboBox.items().find { it.hiddenData?.rowId == gameParams }
        comboBox.selectedItem = item
    }

    override fun getGameParams() = getSelectedTemplate()?.rowId ?: ""

    private fun getSelectedTemplate() = comboBox.selectedItemTyped().hiddenData

    override fun getFilterDesc(): String
    {
        val template = getSelectedTemplate()

        return if (template != null)
        {
            "games for template '${template.name}'"
        }
        else
        {
            "custom games"
        }
    }

    override fun enableChildren(enabled: Boolean)
    {
        comboBox.isEnabled = enabled
    }

    override fun addActionListener(listener: ActionListener)
    {
        comboBox.addActionListener(listener)
    }

    override fun removeActionListener(listener: ActionListener)
    {
        comboBox.removeActionListener(listener)
    }

}