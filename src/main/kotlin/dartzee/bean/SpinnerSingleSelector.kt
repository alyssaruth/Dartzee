package dartzee.bean

import java.awt.Dimension
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class SpinnerSingleSelector : JSpinner(), ChangeListener {
    init {
        setSize(50, 25)
        preferredSize = Dimension(50, 25)
        model = SpinnerNumberModel(20, 1, 25, 1)
        addChangeListener(this)
    }

    /**
     * This looks a bit weird, but what we want is:
     * - Upping from 20 -> 25
     * - Downing from 25 -> 20
     * - Should not be able to enter 21-24 manually.
     */
    override fun stateChanged(arg0: ChangeEvent) {
        val intVal = value as Int
        when (intVal) {
            21,
            22 -> value = 25
            23,
            24 -> value = 20
        }
    }
}
