package dartzee.helper

import dartzee.`object`.SEGMENT_TYPE_DOUBLE
import dartzee.`object`.SEGMENT_TYPE_MISS
import dartzee.`object`.SEGMENT_TYPE_OUTER_SINGLE
import dartzee.`object`.SEGMENT_TYPE_TREBLE
import dartzee.core.util.DateStatics
import dartzee.core.util.FileUtil
import dartzee.core.util.getSqlDateNow
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.db.*
import dartzee.game.GameType
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.DatabaseUtil
import dartzee.utils.DatabaseUtil.Companion.executeQueryAggregate
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob

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
                     matchParams: String = ""): DartsMatchEntity
{
    val m = DartsMatchEntity()
    m.rowId = uuid
    m.localId = localId
    m.games = games
    m.mode = mode
    m.dtFinish = dtFinish
    m.matchParams = matchParams

    m.saveToDatabase()
    return m
}


fun insertPlayer(uuid: String = randomGuid(),
                 name: String = "Clive",
                 strategy: Int = 1,
                 strategyXml: String = "",
                 dtDeleted: Timestamp = DateStatics.END_OF_TIME): PlayerEntity
{

    val playerImageId = insertPlayerImage().rowId

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
                      dtFinished: Timestamp = DateStatics.END_OF_TIME,
                      insertPlayer: Boolean = false): ParticipantEntity
{
    val pe = ParticipantEntity()
    pe.rowId = uuid
    pe.gameId = gameId
    pe.playerId = playerId
    pe.ordinal = ordinal
    pe.finishingPosition = finishingPosition
    pe.finalScore = finalScore
    pe.dtFinished = dtFinished

    if (insertPlayer)
    {
        pe.playerId = insertPlayer().rowId
    }

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
               segmentType: Int = getSegmentTypeForMultiplier(multiplier),
               dtCreation: Timestamp = getSqlDateNow(),
               dtLastUpdate: Timestamp = getSqlDateNow()): DartEntity
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

    drt.saveToDatabase(dtLastUpdate)

    return drt
}
private fun getSegmentTypeForMultiplier(multiplier: Int) = when(multiplier)
{
    1 -> SEGMENT_TYPE_OUTER_SINGLE
    2 -> SEGMENT_TYPE_DOUBLE
    3 -> SEGMENT_TYPE_TREBLE
    else -> SEGMENT_TYPE_MISS
}

fun insertGameForReport(uuid: String = randomGuid(),
                        localId: Long = LocalIdGenerator.generateLocalId("Game"),
                        gameType: GameType = GameType.X01,
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
               gameType: GameType = GameType.X01,
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

fun insertDartzeeRule(uuid: String = randomGuid(),
                      entityName: String = "",
                      entityId: String = "",
                      ordinal: Int = 1,
                      calculationResult: DartzeeRuleCalculationResult = makeDartzeeRuleCalculationResult(),
                      dtCreation: Timestamp = getSqlDateNow(),
                      dtLastUpdate: Timestamp = getSqlDateNow()): DartzeeRuleEntity
{
    val de = DartzeeRuleEntity()
    de.rowId = uuid
    de.dtCreation = dtCreation
    de.entityId = entityId
    de.entityName = entityName
    de.calculationResult = calculationResult.toDbString()
    de.ordinal = ordinal

    de.saveToDatabase(dtLastUpdate)

    return de
}

fun insertDartzeeTemplate(uuid: String = randomGuid(),
                      name: String = "Template",
                      dtCreation: Timestamp = getSqlDateNow(),
                      dtLastUpdate: Timestamp = getSqlDateNow()): DartzeeTemplateEntity
{
    val de = DartzeeTemplateEntity()
    de.rowId = uuid
    de.dtCreation = dtCreation
    de.name = name

    de.saveToDatabase(dtLastUpdate)

    return de
}

fun insertTemplateAndRule(name: String = "Template"): DartzeeTemplateEntity
{
    val template = insertDartzeeTemplate(name = name)
    insertDartzeeRule(entityName = DARTZEE_TEMPLATE, entityId = template.rowId)
    return template
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

private val fileBytes = FileUtil.getByteArrayForResource("/avatars/BaboOne.png")
private val serialBlob = SerialBlob(fileBytes)
fun insertPlayerImage(): PlayerImageEntity
{
    val pi = PlayerImageEntity()
    pi.assignRowId()
    pi.blobData = serialBlob
    pi.filepath = "rsrc:/avatars/BaboOne.png"
    pi.bytes = fileBytes
    pi.preset = false

    pi.saveToDatabase()
    return pi
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