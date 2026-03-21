package dartzee.theme

import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.selectedItemTyped
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ComboBoxTheme : JComboBox<ComboBoxItem<Theme?>>() {
    init {
        val model = DefaultComboBoxModel<ComboBoxItem<Theme?>>()
        model.addElement(ComboBoxItem<Theme?>(null, "Classic (no theme)"))

        themeMap().forEach { (_, theme) ->
            val item = ComboBoxItem<Theme?>(theme, theme.nameCapitalised())
            model.addElement(item)
        }

        setModel(model)
    }

    fun selectedTheme() = selectedItemTyped().hiddenData
}
