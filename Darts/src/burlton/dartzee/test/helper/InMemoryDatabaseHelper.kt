package burlton.dartzee.test.helper

import burlton.dartzee.code.db.*
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.DatabaseUtil.Companion.executeQueryAggregate
import burlton.desktopcore.code.util.DateStatics
import java.sql.Timestamp
import java.util.*

fun wipeTable(tableName: String)
{
    DatabaseUtil.executeUpdate("DELETE FROM $tableName")
}

fun randomGuid() = UUID.randomUUID().toString()

fun insertDartsMatch(uuid: String = randomGuid(),
                     localId: Long = LocalIdGenerator.generateLocalId("DartsMatch"),
                     games: Int = 3,
                     mode: Int = DartsMatchEntity.MODE_FIRST_TO,
                     dtFinish: Timestamp = DateStatics.END_OF_TIME,
                     matchParams: String = ""): String
{
    val m = DartsMatchEntity()
    m.rowId = uuid
    m.localId = localId
    m.games = games
    m.mode = mode
    m.dtFinish = dtFinish
    m.matchParams = matchParams

    m.saveToDatabase()
    return m.rowId
}


fun insertPlayer(uuid: String = randomGuid(),
                 name: String = "Clive",
                 strategy: Int = 1,
                 strategyXml: String = "",
                 dtDeleted: Timestamp = DateStatics.END_OF_TIME,
                 playerImageId: String = randomGuid()): String
{
    val p = PlayerEntity()
    p.rowId = uuid
    p.name = name
    p.strategy = strategy
    p.strategyXml = strategyXml
    p.dtDeleted = dtDeleted
    p.playerImageId = playerImageId

    p.saveToDatabase()
    return p.rowId
}

fun insertParticipant(uuid: String = randomGuid(),
                      gameId: String = randomGuid(),
                      playerId: String = randomGuid(),
                      ordinal: Int = 1,
                      finishingPosition: Int = -1,
                      finalScore: Int = -1,
                      dtFinished: Timestamp = DateStatics.END_OF_TIME): String
{
    val pe = ParticipantEntity()
    pe.rowId = uuid
    pe.gameId = gameId
    pe.playerId = playerId
    pe.ordinal = ordinal
    pe.finishingPosition = finishingPosition
    pe.finalScore = finalScore
    pe.dtFinished = dtFinished

    pe.saveToDatabase()

    return pe.rowId
}

fun insertGame(uuid: String = randomGuid(),
               localId: Long = LocalIdGenerator.generateLocalId("Game"),
               gameType: Int = GAME_TYPE_X01,
               gameParams: String = "501",
               dtFinish: Timestamp = DateStatics.END_OF_TIME,
               dartsMatchId: String = "",
               matchOrdinal: Int = -1): String
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

    return uuid
}

fun getCountFromTable(table: String): Int
{
    return executeQueryAggregate("SELECT COUNT(1) FROM $table")
}