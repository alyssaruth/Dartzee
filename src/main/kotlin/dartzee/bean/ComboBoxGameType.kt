package dartzee.bean

import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.selectedItemTyped
import dartzee.utils.getAllGameTypes
import dartzee.utils.getTypeDesc
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ComboBoxGameType : JComboBox<ComboBoxItem<Int>>()
{
    init
    {
        val model = DefaultComboBoxModel<ComboBoxItem<Int>>()

        val gameTypes = getAllGameTypes()
        for (gameType in gameTypes)
        {
            val item = ComboBoxItem(gameType, getTypeDesc(gameType))
            model.addElement(item)
        }

        setModel(model)
    }

    fun getGameType() = selectedItemTyped().hiddenData
}
