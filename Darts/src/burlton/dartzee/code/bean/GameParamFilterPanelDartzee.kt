package burlton.dartzee.code.bean

import burlton.dartzee.code.db.DartzeeTemplateEntity
import burlton.desktopcore.code.bean.ComboBoxItem
import burlton.desktopcore.code.bean.items
import burlton.desktopcore.code.bean.selectedItemTyped
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.JComboBox

class GameParamFilterPanelDartzee: GameParamFilterPanel()
{
    private val comboBox = JComboBox<ComboBoxItem<DartzeeTemplateEntity?>>()

    init
    {
        add(comboBox, BorderLayout.CENTER)

        populateComboBox()
    }

    private fun populateComboBox()
    {
        val templates = DartzeeTemplateEntity().retrieveEntities()

        val divider = ComboBoxItem<DartzeeTemplateEntity?>(null, "-----")
        divider.isEnabled = false

        comboBox.addItem(ComboBoxItem(null, "Custom"))
        comboBox.addItem(divider)
        templates.forEach { comboBox.addItem(ComboBoxItem(it, it.name)) }
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
            "games for templates '${template.name}"
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

}