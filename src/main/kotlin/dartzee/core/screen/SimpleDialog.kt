package dartzee.core.screen

import dartzee.logging.CODE_SWING_ERROR
import dartzee.utils.InjectedThings.logger
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
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

        addWindowListener(object : WindowAdapter() {
            override fun windowOpened(e: WindowEvent?) {
                dialogShown()
            }
        })
    }

    /**
     * Abstract methods
     */
    abstract fun okPressed()

    /**
     * Default methods
     */
    open fun dialogShown() {}
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
            else -> logger.error(CODE_SWING_ERROR, "Unexpected button pressed: ${arg0.source}")
        }
    }
}
