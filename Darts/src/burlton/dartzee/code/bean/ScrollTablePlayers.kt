package burlton.dartzee.code.bean

import burlton.dartzee.code.db.PlayerEntity
import burlton.desktopcore.code.bean.ScrollTable
import burlton.desktopcore.code.util.TableUtil

class ScrollTablePlayers : ScrollTable()
{
    init
    {
        initTableModel()

        setRowName("player")
    }

    fun getSelectedPlayer(): PlayerEntity?
    {
        val row = table.selectedRow
        return if (row == -1) null else getPlayerEntityForRow(row)
    }

    fun getAllPlayers(): MutableList<PlayerEntity>
    {
        val ret = mutableListOf<PlayerEntity>()

        for (i in 0 until table.rowCount)
        {
            val player = getPlayerEntityForRow(i)
            ret.add(player)
        }

        return ret
    }


    fun getSelectedPlayers(): MutableList<PlayerEntity>
    {
        val ret = mutableListOf<PlayerEntity>()

        val viewRows = table.selectedRows
        for (i in viewRows.indices)
        {
            val player = getPlayerEntityForRow(viewRows[i])
            ret.add(player)
        }

        return ret
    }

    private fun getPlayerEntityForRow(row: Int): PlayerEntity
    {
        return table.getValueAt(row, 1) as PlayerEntity
    }

    fun initTableModel(players: List<PlayerEntity> = listOf())
    {
        val model = TableUtil.DefaultModel()
        model.addColumn("")
        model.addColumn("Player")

        setModel(model)

        setRowHeight(23)
        setColumnWidths("25")

        addPlayers(players)

        sortBy(1, false)
    }

    fun addPlayers(players: List<PlayerEntity>) = players.forEach{ addPlayer(it) }
    private fun addPlayer(player: PlayerEntity)
    {
        val flag = player.getFlag()
        val row = arrayOf(flag, player)

        addRow(row)
    }
}
