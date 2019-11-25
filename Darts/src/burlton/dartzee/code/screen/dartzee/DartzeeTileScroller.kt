package burlton.dartzee.code.screen.dartzee

import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

class DartzeeTileScroller: JScrollPane()
{
    private val tilePanel = JPanel()

    private var tiles = listOf<DartzeeRuleTile>()

    init
    {
        setViewportView(tilePanel)
        horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER
    }

    fun getTiles() = tiles
    fun setTiles(tiles: List<DartzeeRuleTile>)
    {
        this.tiles = tiles

        tilePanel.removeAll()
        tiles.forEach { tilePanel.add(it) }

        repaint()
    }
}
