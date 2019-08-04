package burlton.dartzee.code.bean

import burlton.dartzee.code.db.GameEntity
import burlton.desktopcore.code.bean.ComboBoxItem
import burlton.desktopcore.code.bean.selectedItemTyped
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ComboBoxGameType : JComboBox<ComboBoxItem<Int>>()
{
    init
    {
        val model = DefaultComboBoxModel<ComboBoxItem<Int>>()

        val gameTypes = GameEntity.getAllGameTypes()
        for (gameType in gameTypes)
        {
            val item = ComboBoxItem(gameType, GameEntity.getTypeDesc(gameType))
            model.addElement(item)
        }

        setModel(model)
    }

    fun getGameType() = selectedItemTyped().hiddenData
}
