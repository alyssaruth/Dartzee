package dartzee.core.bean

import java.awt.event.MouseEvent

interface IHyperlinkListener
{
    fun linkClicked(arg0: MouseEvent)
    fun isOverHyperlink(arg0: MouseEvent): Boolean
}