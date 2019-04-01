package burlton.dartzee.test.helper

import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.LocalIdGenerator
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.DatabaseUtil.Companion.executeQueryAggregate
import burlton.desktopcore.code.util.DateStatics
import java.sql.Timestamp
import java.util.*

fun wipeTable(tableName: String)
{
    DatabaseUtil.executeUpdate("DELETE FROM $tableName")
}

fun insertGame(uuid: String = UUID.randomUUID().toString(),
               localId: Long = LocalIdGenerator.generateLocalId("Game"),
               gameType: Int = GAME_TYPE_X01,
               gameParams: String = "501",
               dtFinish: Timestamp = DateStatics.END_OF_TIME,
               dartsMatchId: String = "",
               matchOrdinal: Int = -1)
{
    val ge = GameEntity()
    ge.rowId = uuid
    ge.localId = localId
    ge.gameType = gameType
    ge.gameParams = gameParams
    ge.dtFinish = dtFinish
    ge.dartsMatchId = dartsMatchId
    ge.matchOrdinal = matchOrdinal

    ge.saveToDatabase()
}

fun getCountFromTable(table: String): Int
{
    return executeQueryAggregate("SELECT COUNT(1) FROM $table")
}