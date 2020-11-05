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
import dartzee.utils.DATABASE_FILE_PATH
import dartzee.utils.Database
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob

fun wipeTable(tableName: String)
{
    mainDatabase.executeUpdate("DELETE FROM $tableName")
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
                     database: Database = mainDatabase,
                     localId: Long = database.generateLocalId("DartsMatch"),
                     games: Int = 3,
                     mode: MatchMode = MatchMode.FIRST_TO,
                     dtFinish: Timestamp = DateStatics.END_OF_TIME,
                     matchParams: String = "",
                     gameParams: String = ""): DartsMatchEntity
{
    val m = DartsMatchEntity(database)
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
                 playerImageId: String? = null,
                 database: Database = mainDatabase): PlayerEntity
{
    val p = PlayerEntity(database)
    p.rowId = uuid
    p.name = name
    p.strategy = strategy
    p.dtDeleted = dtDeleted
    p.playerImageId = playerImageId ?: insertPlayerImage(database = database).rowId

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
                      insertPlayer: Boolean = false,
                      database: Database = mainDatabase): ParticipantEntity
{
    val pe = ParticipantEntity(database)
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
               dtLastUpdate: Timestamp = getSqlDateNow(),
               database: Database = mainDatabase): DartEntity
{
    val drt = DartEntity(database)
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
                        localId: Long = mainDatabase.generateLocalId("Game"),
                        gameType: GameType = GameType.X01,
                        gameParams: String = "501",
                        dtFinish: Timestamp = DateStatics.END_OF_TIME,
                        dartsMatchId: String = "",
                        matchOrdinal: Int = -1,
                        dtCreation: Timestamp = getSqlDateNow()): GameEntity
{
    val game = insertGame(uuid, mainDatabase, localId, gameType, gameParams, dtFinish, dartsMatchId, matchOrdinal, dtCreation)
    val player = insertPlayer()
    insertParticipant(gameId = game.rowId, playerId = player.rowId)

    return game
}

fun insertGame(uuid: String = randomGuid(),
               database: Database = mainDatabase,
               localId: Long = database.generateLocalId("Game"),
               gameType: GameType = GameType.X01,
               gameParams: String = "501",
               dtFinish: Timestamp = DateStatics.END_OF_TIME,
               dartsMatchId: String = "",
               matchOrdinal: Int = -1,
               dtCreation: Timestamp = getSqlDateNow(),
               dtLastUpdate: Timestamp = getSqlDateNow()): GameEntity
{
    val ge = GameEntity(database)
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
                      dtAchieved: Timestamp = getSqlDateNow()): AchievementEntity
{
    val a = AchievementEntity()
    a.rowId = uuid
    a.playerId = playerId
    a.achievementRef = achievementRef
    a.gameIdEarned = gameIdEarned
    a.achievementCounter = achievementCounter
    a.achievementDetail = achievementDetail
    a.dtAchieved = dtAchieved

    a.saveToDatabase()

    return a
}

fun insertPlayerImage(resource: String = "BaboOne", database: Database = mainDatabase): PlayerImageEntity
{
    val fileBytes = FileUtil.getByteArrayForResource("/avatars/$resource.png")
    val pi = PlayerImageEntity(database)
    pi.assignRowId()
    pi.blobData = SerialBlob(fileBytes)
    pi.filepath = "rsrc:/avatars/$resource.png"
    pi.bytes = fileBytes
    pi.preset = false

    pi.saveToDatabase()
    return pi
}


fun getCountFromTable(table: String, database: Database = mainDatabase): Int
{
    return database.executeQueryAggregate("SELECT COUNT(1) FROM $table")
}

/**
 * Retrieve
 */
fun retrieveGame() = GameEntity().retrieveEntities().first()
fun retrieveDart() = DartEntity().retrieveEntities().first()
fun retrieveParticipant() = ParticipantEntity().retrieveEntities().first()
fun retrieveAchievement() = AchievementEntity().retrieveEntities().first()

data class AchievementSummary(val achievementRef: Int, val achievementCounter: Int, val gameIdEarned: String, val achievementDetail: String = "")
fun retrieveAchievementsForPlayer(playerId: String): List<AchievementSummary>
{
    val achievements = AchievementEntity.retrieveAchievements(playerId)
    return achievements.map { AchievementSummary(it.achievementRef, it.achievementCounter, it.gameIdEarned, it.achievementDetail) }
}

private fun makeInMemoryDatabase(dbName: String = UUID.randomUUID().toString(), filePath: String = DATABASE_FILE_PATH): Database
{
    val fullName = "jdbc:derby:memory:$dbName;create=true"
    return Database(filePath = filePath, dbName = fullName).also { it.initialiseConnectionPool(5) }
}

fun usingInMemoryDatabase(dbName: String = UUID.randomUUID().toString(),
                          filePath: String = DATABASE_FILE_PATH,
                          withSchema: Boolean = false,
                          testBlock: (inMemoryDatabase: Database) -> Unit)
{
    val db = makeInMemoryDatabase(dbName, filePath)
    try
    {
        if (withSchema)
        {
            val migrator = DatabaseMigrator(emptyMap())
            migrator.migrateToLatest(db, "Test")
        }

        testBlock(db)
    }
    finally
    {
        db.closeConnectionsAndDrop(dbName)
    }
}

fun Database.closeConnectionsAndDrop(dbName: String)
{
    hsConnections.forEach {
        it.close()
    }

    try
    {
        DriverManager.getConnection("jdbc:derby:memory:$dbName;drop=true")
    }
    catch (sqle: SQLException)
    {
        if (sqle.message != "Database 'memory:$dbName' dropped.")
        {
            InjectedThings.logger.logSqlException("jdbc:derby:memory:$dbName;drop=true", "jdbc:derby:memory:$dbName;drop=true", sqle)
        }
    }
}

fun Database.getTableNames(): List<String>
{
    val sb = StringBuilder()
    sb.append(" SELECT TableName")
    sb.append(" FROM sys.systables")
    sb.append(" WHERE TableType = 'T'")

    val list = mutableListOf<String>()
    executeQuery(sb).use { rs ->
        while (rs.next())
        {
            list.add(rs.getString("TableName"))
        }
    }

    return list.toList()
}