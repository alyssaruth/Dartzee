package dartzee.helper

import dartzee.`object`.SegmentType
import dartzee.ai.DartsAiModel
import dartzee.core.util.DateStatics
import dartzee.core.util.FileUtil
import dartzee.core.util.getSqlDateNow
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.db.*
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.InjectedThings.database
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob

fun wipeTable(tableName: String)
{
    database.executeUpdate("DELETE FROM $tableName")
}

fun randomGuid() = UUID.randomUUID().toString()

fun insertPlayerForGame(name: String, gameId: String, strategy: String = "foo"): PlayerEntity
{
    val player = insertPlayer(name = name, strategy = strategy)
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
                     localId: Long = LocalIdGenerator.generateLocalId(database, "DartsMatch"),
                     games: Int = 3,
                     mode: MatchMode = MatchMode.FIRST_TO,
                     dtFinish: Timestamp = DateStatics.END_OF_TIME,
                     matchParams: String = "",
                     gameParams: String = ""): DartsMatchEntity
{
    val m = DartsMatchEntity()
    m.rowId = uuid
    m.localId = localId
    m.games = games
    m.mode = mode
    m.dtFinish = dtFinish
    m.matchParams = matchParams
    m.gameParams = gameParams

    m.saveToDatabase()
    return m
}

fun insertGameForPlayer(player: PlayerEntity,
                        gameType: GameType = GameType.X01,
                        finalScore: Int = -1,
                        dtFinished: Timestamp = DateStatics.END_OF_TIME)
{
    val game = insertGame(gameType = gameType)
    insertParticipant(playerId = player.rowId, gameId = game.rowId, finalScore = finalScore, dtFinished = dtFinished)
}

fun insertPlayer(model: DartsAiModel, name: String = "Clive") =
        insertPlayer(strategy = model.toJson(), name = name)

fun insertPlayer(uuid: String = randomGuid(),
                 name: String = "Clive",
                 strategy: String = "",
                 dtDeleted: Timestamp = DateStatics.END_OF_TIME,
                 playerImageId: String? = null): PlayerEntity
{
    val p = PlayerEntity()
    p.rowId = uuid
    p.name = name
    p.strategy = strategy
    p.dtDeleted = dtDeleted
    p.playerImageId = playerImageId ?: insertPlayerImage().rowId

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
               segmentType: SegmentType = getSegmentTypeForMultiplier(multiplier),
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
fun getSegmentTypeForMultiplier(multiplier: Int) = when(multiplier)
{
    1 -> SegmentType.OUTER_SINGLE
    2 -> SegmentType.DOUBLE
    3 -> SegmentType.TREBLE
    else -> SegmentType.MISS
}

fun insertGameForReport(uuid: String = randomGuid(),
                        localId: Long = LocalIdGenerator.generateLocalId(database, "Game"),
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
               localId: Long = LocalIdGenerator.generateLocalId(database, "Game"),
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

fun insertPlayerImage(resource: String = "BaboOne"): PlayerImageEntity
{
    val fileBytes = FileUtil.getByteArrayForResource("/avatars/$resource.png")
    val pi = PlayerImageEntity()
    pi.assignRowId()
    pi.blobData = SerialBlob(fileBytes)
    pi.filepath = "rsrc:/avatars/$resource.png"
    pi.bytes = fileBytes
    pi.preset = false

    pi.saveToDatabase()
    return pi
}


fun getCountFromTable(table: String): Int
{
    return database.executeQueryAggregate("SELECT COUNT(1) FROM $table")
}

fun dropAllTables() = dropTables(false)
fun dropUnexpectedTables() = dropTables(true)

fun dropTables(onlyUnexpected: Boolean): List<String>
{
    val entities = DartsDatabaseUtil.getAllEntitiesIncludingVersion()
    val tableNameSql = entities.joinToString{ "'${it.getTableNameUpperCase()}'"}

    val sb = StringBuilder()
    sb.append(" SELECT TableName")
    sb.append(" FROM sys.systables")
    sb.append(" WHERE TableType = 'T'")

    if (onlyUnexpected)
    {
        sb.append(" AND TableName NOT IN ($tableNameSql)")
    }

    val list = mutableListOf<String>()
    database.executeQuery(sb).use{ rs ->
        while (rs.next())
        {
            list.add(rs.getString("TableName"))
        }
    }

    list.forEach{ database.executeUpdate("DROP TABLE $it")}

    return list
}

/**
 * Retrieve
 */
fun retrieveGame() = GameEntity().retrieveEntities().first()
fun retrieveDart() = DartEntity().retrieveEntities().first()
fun retrieveParticipant() = ParticipantEntity().retrieveEntities().first()
fun retrieveAchievement() = AchievementEntity().retrieveEntities().first()
fun retrieveParticipant(playerId: String) = ParticipantEntity().retrieveEntities("PlayerId = '$playerId'").first()

data class AchievementSummary(val achievementRef: Int, val achievementCounter: Int, val gameIdEarned: String, val achievementDetail: String = "")
fun retrieveAchievementsForPlayer(playerId: String): List<AchievementSummary>
{
    val achievements = AchievementEntity.retrieveAchievements(playerId)
    return achievements.map { AchievementSummary(it.achievementRef, it.achievementCounter, it.gameIdEarned, it.achievementDetail) }
}