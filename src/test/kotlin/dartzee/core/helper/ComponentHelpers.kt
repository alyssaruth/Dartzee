package dartzee.core.helper

import com.github.alyssaburlton.swingtest.doubleClick
import com.github.alyssaburlton.swingtest.processKeyPress
import dartzee.core.bean.ScrollTable
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel

/** Test methods */
fun ScrollTable.processKeyPress(key: Int) {
    this.table.processKeyPress(key)
}

fun ScrollTable.doubleClick() {
    this.table.doubleClick()
}

fun JLabel.getIconImage(): BufferedImage = (icon as ImageIcon).image as BufferedImage
