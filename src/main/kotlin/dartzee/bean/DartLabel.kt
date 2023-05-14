package dartzee.bean

import dartzee.utils.ResourceCache
import javax.swing.JLabel

class DartLabel : JLabel(ResourceCache.DART_IMG)
{
    init
    {
        setSize(76, 80)
    }
}