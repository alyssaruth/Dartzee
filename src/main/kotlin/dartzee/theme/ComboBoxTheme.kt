package dartzee.theme

import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.selectedItemTyped
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox

class ComboBoxTheme : JComboBox<ComboBoxItem<ThemeId>>() {
    init {
        val model = DefaultComboBoxModel<ComboBoxItem<ThemeId>>()

        ThemeId.values().forEach { themeId ->
            val theme = themeMap()[themeId]
            val name = theme?.name ?: "Classic (no theme)"
            model.addElement(ComboBoxItem(themeId, name))
        }

        setModel(model)
    }

    fun selectedTheme() = selectedItemTyped().hiddenData
}
