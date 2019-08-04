package burlton.desktopcore.code.bean

class ComboBoxItem<E>(val hiddenData: E, private val visibleData: Any)
{
    var isEnabled = true

    override fun toString() = if (isEnabled) "$visibleData" else "<html><font color=\"gray\">$visibleData</font></html>"
}
