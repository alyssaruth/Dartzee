package burlton.desktopcore.code.screen

import burlton.core.code.util.Debug
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JPanel

abstract class SimpleDialog : JDialog(), ActionListener
{
    val panelOkCancel = JPanel()
    val btnOk = JButton("Ok")
    val btnCancel = JButton("Cancel")

    init
    {
        contentPane.add(panelOkCancel, BorderLayout.SOUTH)

        panelOkCancel.add(btnOk)
        panelOkCancel.add(btnCancel)

        btnCancel.isVisible = allowCancel()

        btnOk.addActionListener(this)
        btnCancel.addActionListener(this)
    }

    /**
     * Abstract methods
     */
    abstract fun okPressed()

    /**
     * Default methods
     */
    open fun allowCancel() = true
    open fun cancelPressed()
    {
        dispose()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        when (arg0.source)
        {
            btnOk -> okPressed()
            btnCancel -> cancelPressed()
            else -> Debug.stackTrace("Unexpected button pressed: ${arg0.source}")
        }
    }
}
