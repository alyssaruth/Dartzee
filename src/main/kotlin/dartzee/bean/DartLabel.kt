package dartzee.bean

import dartzee.utils.ResourceCache
import javax.swing.ImageIcon
import javax.swing.JLabel

class DartLabel(icon: ImageIcon = ResourceCache.DART_IMG) : JLabel(icon) {
    init {
        setSize(icon.iconWidth, icon.iconHeight)
    }
}
