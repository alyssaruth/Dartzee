package dartzee.core.screen

import dartzee.core.bean.IColourSelector
import java.awt.Color
import java.awt.event.ActionListener
import javax.swing.JColorChooser
import javax.swing.colorchooser.DefaultColorSelectionModel

class ColourChooserDialog : SimpleDialog(), ActionListener, IColourSelector
{
    var initialColour: Color = Color.BLACK
    var selectedColour: Color = Color.BLACK

    val colourChooser = JColorChooser(DefaultColorSelectionModel())

    init
    {
        title = "Choose Colour"
        setSize(660, 450)
        contentPane.add(colourChooser)
        isModal = true

        btnOk.addActionListener(this)
        btnCancel.addActionListener(this)
    }

    override fun okPressed()
    {
        selectedColour = colourChooser.color
        dispose()
    }

    override fun cancelPressed()
    {
        selectedColour = initialColour
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