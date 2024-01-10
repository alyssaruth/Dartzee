package dartzee.core.bean

data class ComboBoxItem<E>(val hiddenData: E, val visibleData: Any, var isEnabled: Boolean = true) {
    override fun toString() =
        if (isEnabled) "$visibleData" else "<html><font color=\"gray\">$visibleData</font></html>"
}
