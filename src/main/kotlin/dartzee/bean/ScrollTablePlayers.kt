package dartzee.bean

import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil
import dartzee.db.PlayerEntity

fun ScrollTable.getSelectedPlayer(): PlayerEntity? {
    val row = table.selectedRow
    return if (row == -1) null else getPlayerEntityForRow(row)
}

fun ScrollTable.getAllPlayers() = (0 until table.rowCount).map(::getPlayerEntityForRow)

fun ScrollTable.getSelectedPlayers() = table.selectedRows.map(::getPlayerEntityForRow)

fun ScrollTable.getPlayerEntityForRow(row: Int) = table.getValueAt(row, 1) as PlayerEntity

fun ScrollTable.initPlayerTableModel(players: List<PlayerEntity> = listOf()) {
    val model = TableUtil.DefaultModel()
    model.addColumn("")
    model.addColumn("Player")

    this.model = model

    setRowHeight(23)
    setColumnWidths("25")

    addPlayers(players)

    setRowName("player")
    sortBy(1, false)
}

fun ScrollTable.addPlayers(players: List<PlayerEntity>) = players.forEach(::addPlayer)

private fun ScrollTable.addPlayer(player: PlayerEntity) {
    val flag = player.getFlag()
    val row = arrayOf(flag, player)

    addRow(row)
}
