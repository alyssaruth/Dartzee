package burlton.desktopcore.code.screen

import burlton.desktopcore.code.bean.IColourSelector
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JColorChooser
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.colorchooser.DefaultColorSelectionModel

class ColourChooserDialog : JDialog(), ActionListener, IColourSelector
{
    private var initialColour: Color = Color.BLACK
    var selectedColour: Color = Color.BLACK

    private val colourChooser = JColorChooser(DefaultColorSelectionModel())
    private val btnOk = JButton("Ok")
    private val btnCancel = JButton("Cancel")

    init
    {
        title = "Choose Colour"
        setSize(660, 450)
        contentPane.add(colourChooser)
        val panel = JPanel()
        contentPane.add(panel, BorderLayout.SOUTH)
        isModal = true

        panel.add(btnOk)
        panel.add(btnCancel)
        btnOk.addActionListener(this)
        btnCancel.addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent)
    {
        when (e.source)
        {
            btnOk -> selectedColour = colourChooser.color
            btnCancel -> selectedColour = initialColour
        }

        dispose()
    }

    override fun selectColour(initialColour: Color): Color
    {
        this.initialColour = initialColour
        colourChooser.color = initialColour

        isVisible = true

        return selectedColour
    }
}