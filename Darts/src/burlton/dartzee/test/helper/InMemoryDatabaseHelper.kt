package burlton.dartzee.test.helper

import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.db.*
import burlton.dartzee.code.utils.DartsDatabaseUtil
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.code.utils.DatabaseUtil.Companion.executeQueryAggregate
import burlton.desktopcore.code.util.DateStatics
import burlton.desktopcore.code.util.getSqlDateNow
import java.sql.Timestamp
import java.util.*

fun wipeTable(tableName: String)
{
    DatabaseUtil.executeUpdate("DELETE FROM $tableName")
}

fun randomGuid() = UUID.randomUUID().toString()

fun insertPlayerForGame(name: String, gameId: String): PlayerEntity
{
    val player = insertPlayer(name = name)
    insertParticipant(playerId = player.rowId, gameId = gameId)
    return player
}

fun factoryPlayer(name: String): PlayerEntity
{
    val p = PlayerEntity()
    p.name = name
    p.assignRowId()
    return p
}

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
                 playerImageId: String = randomGuid()): PlayerEntity
{
    val p = PlayerEntity()
    p.rowId = uuid
    p.name = name
    p.strategy = strategy
    p.strategyXml = strategyXml
    p.dtDeleted = dtDeleted
    p.playerImageId = playerImageId

    p.saveToDatabase()
    return p
}

fun insertParticipant(uuid: String = randomGuid(),
                      gameId: String = randomGuid(),
                      playerId: String = randomGuid(),
                      ordinal: Int = 1,
                      finishingPosition: Int = -1,
                      finalScore: Int = -1,
                      dtFinished: Timestamp = DateStatics.END_OF_TIME): ParticipantEntity
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

    return pe
}

fun insertDart(participant: ParticipantEntity,
               uuid: String = randomGuid(),
               roundNumber: Int = 1,
               ordinal: Int = 1,
               startingScore: Int = 501,
               score: Int = 20,
               multiplier: Int = 3,
               posX: Int = 20,
               posY: Int = 20,
               segmentType: Int = SEGMENT_TYPE_TREBLE,
               dtCreation: Timestamp = getSqlDateNow()): DartEntity
{
    val drt = DartEntity()
    drt.dtCreation = dtCreation
    drt.rowId = uuid
    drt.playerId = participant.playerId
    drt.participantId = participant.rowId
    drt.roundNumber = roundNumber
    drt.ordinal = ordinal
    drt.startingScore = startingScore
    drt.multiplier = multiplier
    drt.score = score
    drt.posX = posX
    drt.posY = posY
    drt.segmentType = segmentType

    drt.saveToDatabase()

    return drt
}

fun insertGameForReport(uuid: String = randomGuid(),
                        localId: Long = LocalIdGenerator.generateLocalId("Game"),
                        gameType: Int = GAME_TYPE_X01,
                        gameParams: String = "501",
                        dtFinish: Timestamp = DateStatics.END_OF_TIME,
                        dartsMatchId: String = "",
                        matchOrdinal: Int = -1,
                        dtCreation: Timestamp = getSqlDateNow()): GameEntity
{
    val game = insertGame(uuid, localId, gameType, gameParams, dtFinish, dartsMatchId, matchOrdinal, dtCreation)
    val player = insertPlayer()
    insertParticipant(gameId = game.rowId, playerId = player.rowId)

    return game
}

fun insertGame(uuid: String = randomGuid(),
               localId: Long = LocalIdGenerator.generateLocalId("Game"),
               gameType: Int = GAME_TYPE_X01,
               gameParams: String = "501",
               dtFinish: Timestamp = DateStatics.END_OF_TIME,
               dartsMatchId: String = "",
               matchOrdinal: Int = -1,
               dtCreation: Timestamp = getSqlDateNow(),
               dtLastUpdate: Timestamp = getSqlDateNow()): GameEntity
{
    val ge = GameEntity()
    ge.rowId = uuid
    ge.localId = localId
    ge.gameType = gameType
    ge.gameParams = gameParams
    ge.dtFinish = dtFinish
    ge.dartsMatchId = dartsMatchId
    ge.matchOrdinal = matchOrdinal
    ge.dtCreation = dtCreation

    ge.saveToDatabase(dtLastUpdate)

    return ge
}

fun insertAchievement(uuid: String = randomGuid(),
                      playerId: String = randomGuid(),
                      achievementRef: Int = -1,
                      gameIdEarned: String = "",
                      achievementCounter: Int = -1,
                      achievementDetail: String = "",
                      dtLastUpdate: Timestamp = getSqlDateNow()): AchievementEntity
{
    val a = AchievementEntity()
    a.rowId = uuid
    a.playerId = playerId
    a.achievementRef = achievementRef
    a.gameIdEarned = gameIdEarned
    a.achievementCounter = achievementCounter
    a.achievementDetail = achievementDetail

    a.saveToDatabase(dtLastUpdate)

    return a
}

fun getCountFromTable(table: String): Int
{
    return executeQueryAggregate("SELECT COUNT(1) FROM $table")
}

fun dropUnexpectedTables(): List<String>
{
    val entities = DartsDatabaseUtil.getAllEntitiesIncludingVersion()
    val tableNameSql = entities.joinToString{ "'${it.getTableNameUpperCase()}'"}

    val sb = StringBuilder()
    sb.append(" SELECT TableName")
    sb.append(" FROM sys.systables")
    sb.append(" WHERE TableType = 'T'")
    sb.append(" AND TableName NOT IN ($tableNameSql)")

    val list = mutableListOf<String>()
    DatabaseUtil.executeQuery(sb).use{ rs ->
        while (rs.next())
        {
            list.add(rs.getString("TableName"))
        }
    }

    list.forEach{ DatabaseUtil.executeUpdate("DROP TABLE $it")}

    return list
}

/**
 * Retrieve
 */
fun retrieveGame() = GameEntity().retrieveEntities().first()
fun retrieveDart() = DartEntity().retrieveEntities().first()
fun retrieveParticipant() = ParticipantEntity().retrieveEntities().first()
fun retrieveAchievement() = AchievementEntity().retrieveEntities().first()