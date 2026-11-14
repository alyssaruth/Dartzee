package dartzee.theme

import javax.swing.JComponent

abstract class AbstractMenuAnimation : JComponent() {
    var active = false

    abstract fun start()

    abstract fun reset()
}
