package dartzee.bean

import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.selectedItemTyped
import dartzee.db.GameType
import dartzee.utils.getTypeDesc
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ComboBoxGameType : JComboBox<ComboBoxItem<GameType>>()
{
    init
    {
        val model = DefaultComboBoxModel<ComboBoxItem<GameType>>()

        val gameTypes = GameType.values()
        for (gameType in gameTypes)
        {
            val item = ComboBoxItem(gameType, getTypeDesc(gameType))
            model.addElement(item)
        }

        setModel(model)
    }

    fun getGameType() = selectedItemTyped().hiddenData
}
