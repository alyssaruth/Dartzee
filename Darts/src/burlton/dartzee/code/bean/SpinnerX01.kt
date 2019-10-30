package burlton.dartzee.code.bean

import java.awt.event.ActionEvent
import java.awt.event.ActionListener

import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class SpinnerX01 : JSpinner(), ChangeListener
{
    private var listener: ActionListener? = null

    init
    {
        model = SpinnerNumberModel(501, 101, 701, 100)
        addChangeListener(this)
    }

    override fun stateChanged(arg0: ChangeEvent)
    {
        val intVal = value as Int
        if (intVal % 100 != 1)
        {
            value = 501
        }

        if (listener != null)
        {
            listener!!.actionPerformed(ActionEvent(this, ActionEvent.ACTION_PERFORMED, null))
        }
    }

    fun addActionListener(listener: ActionListener)
    {
        this.listener = listener
    }

    fun removeActionListener()
    {
        this.listener = null
    }
}
