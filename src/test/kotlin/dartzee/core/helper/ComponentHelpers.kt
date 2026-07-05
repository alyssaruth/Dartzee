package dartzee.core.helper

import dartzee.core.bean.ScrollTable
import io.github.alyssaruth.swingtest.doubleClick
import io.github.alyssaruth.swingtest.processKeyPress
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel

/** Test methods */
fun ScrollTable.processKeyPress(key: Int, async: Boolean = false) {
    this.table.processKeyPress(key, async)
}

fun ScrollTable.doubleClick() {
    this.table.doubleClick()
}

fun JLabel.getIconImage(): BufferedImage = (icon as ImageIcon).image as BufferedImage
