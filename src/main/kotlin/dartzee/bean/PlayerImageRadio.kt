package dartzee.bean

import dartzee.core.util.setMargins
import dartzee.db.PlayerImageEntity
import java.awt.Color
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.border.LineBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

/**
 * Wrap up a PlayerImage so we can render the icon, and store its ID to point a PlayerEntity at it
 */
class PlayerImageRadio(pi: PlayerImageEntity) :
    JPanel(), ChangeListener, IMouseListener, FocusListener {
    var playerImageId = ""
    val rdbtn = JRadioButton()
    val lblImg = JLabel()

    init {
        setMargins(1)
        lblImg.icon = pi.asImageIcon()
        playerImageId = pi.rowId

        add(rdbtn)
        add(lblImg)

        rdbtn.addFocusListener(this)
        rdbtn.addChangeListener(this)
        lblImg.addMouseListener(this)
    }

    fun isSelected() = rdbtn.isSelected

    fun addToButtonGroup(bg: ButtonGroup) {
        bg.add(rdbtn)
    }

    override fun stateChanged(arg0: ChangeEvent) = updateBorder()

    override fun focusLost(e: FocusEvent?) = updateBorder()

    override fun focusGained(e: FocusEvent?) = updateBorder()

    private fun updateBorder() {
        if (rdbtn.isSelected) {
            border = LineBorder(Color.BLACK)
        } else if (rdbtn.hasFocus()) {
            border = BorderFactory.createDashedBorder(Color.GRAY)
        } else {
            setMargins(1)
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        rdbtn.isSelected = true
    }
}
