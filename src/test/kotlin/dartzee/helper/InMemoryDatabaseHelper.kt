package dartzee.helper

import dartzee.achievements.AchievementType
import dartzee.ai.DartsAiModel
import dartzee.core.util.DateStatics
import dartzee.core.util.FileUtil
import dartzee.core.util.getSqlDateNow
import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.db.AchievementEntity
import dartzee.db.DartEntity
import dartzee.db.DartsMatchEntity
import dartzee.db.DartzeeRoundResultEntity
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.DatabaseMigrator
import dartzee.db.DeletionAuditEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.db.PlayerImageEntity
import dartzee.db.TeamEntity
import dartzee.db.X01FinishEntity
import dartzee.game.FinishType
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.game.X01Config
import dartzee.logging.LoggingCode
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.databaseDirectory
import dartzee.utils.InjectedThings.mainDatabase
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp
import java.util.*
import javax.sql.rowset.serial.SerialBlob

fun wipeTable(entityName: EntityName) {
    mainDatabase.executeUpdate("DELETE FROM $entityName")
}

fun randomGuid() = UUID.randomUUID().toString()

fun insertPlayerForGame(name: String, gameId: String, strategy: String = "foo"): PlayerEntity {
    val player = insertPlayer(name = name, strategy = strategy)
    insertParticipant(playerId = player.rowId, gameId = gameId)
    return player
}

fun insertFinishedParticipant(
    name: String,
    gameType: GameType,
    finalScore: Int,
    gameParams: String = DEFAULT_X01_CONFIG.toJson(),
    ai: Boolean = true,
): GameEntity {
    val p = insertPlayer(name = name, strategy = if (ai) DartsAiModel.new().toJson() else "")
    val g = insertGame(gameType = gameType, gameParams = gameParams)
    insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = finalScore)
    return g
}

fun insertFinishedTeam(
    name1: String,
    name2: String,
    gameType: GameType,
    finalScore: Int,
    gameParams: String = DEFAULT_X01_CONFIG.toJson(),
    p1Ai: Boolean = true,
    p2Ai: Boolean = true
): GameEntity {
    val p = insertPlayer(name = name1, strategy = if (p1Ai) DartsAiModel.new().toJson() else "")
    val p2 = insertPlayer(name = name2, strategy = if (p2Ai) DartsAiModel.new().toJson() else "")
    val g = insertGame(gameType = gameType, gameParams = gameParams)
    val t = insertTeam(gameId = g.rowId, finalScore = finalScore)
    insertParticipant(playerId = p.rowId, teamId = t.rowId, ordinal = 0)
    insertParticipant(playerId = p2.rowId, teamId = t.rowId, ordinal = 1)
    return g
}

fun factoryPlayer(name: String): PlayerEntity {
    val p = PlayerEntity()
    p.name = name
    p.assignRowId()
    return p
}

fun insertDartsMatch(
    uuid: String = randomGuid(),
    database: Database = mainDatabase,
    localId: Long = database.generateLocalId(EntityName.DartsMatch),
    games: Int = 3,
    mode: MatchMode = MatchMode.FIRST_TO,
    dtFinish: Timestamp = DateStatics.END_OF_TIME,
    matchParams: String = "",
    gameParams: String = ""
): DartsMatchEntity {
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

fun insertGameForPlayer(
    player: PlayerEntity,
    gameType: GameType = GameType.X01,
    finalScore: Int = -1,
    dtFinished: Timestamp = DateStatics.END_OF_TIME
) {
    val game = insertGame(gameType = gameType)
    insertParticipant(
        playerId = player.rowId,
        gameId = game.rowId,
        finalScore = finalScore,
        dtFinished = dtFinished
    )
}

fun insertPlayer(model: DartsAiModel, name: String = "Clive") =
    insertPlayer(strategy = model.toJson(), name = name)

fun insertPlayer(
    uuid: String = randomGuid(),
    name: String = "Clive",
    strategy: String = "",
    dtDeleted: Timestamp = DateStatics.END_OF_TIME,
    playerImageId: String? = null,
    database: Database = mainDatabase
): PlayerEntity {
    val p = PlayerEntity(database)
    p.rowId = uuid
    p.name = name
    p.strategy = strategy
    p.dtDeleted = dtDeleted
    p.playerImageId = playerImageId ?: insertPlayerImage(database = database).rowId

    p.saveToDatabase()
    return p
}

fun preparePlayers(count: Int): List<PlayerEntity> {
    val p1 = insertPlayer(name = "Alice")
    val p2 = insertPlayer(name = "Bob")
    val p3 = insertPlayer(name = "Clara")
    val p4 = insertPlayer(name = "David")
    val p5 = insertPlayer(name = "Ellie")

    return listOf(p1, p2, p3, p4, p5).subList(0, count)
}

fun insertFinishForPlayer(
    player: PlayerEntity,
    finish: Int,
    dtCreation: Timestamp = getSqlDateNow(),
    game: GameEntity = insertGame(gameType = GameType.X01),
    database: Database = mainDatabase
): GameEntity {
    val entity = X01FinishEntity(database)
    entity.assignRowId()
    entity.playerId = player.rowId
    entity.gameId = game.rowId
    entity.finish = finish
    entity.dtCreation = dtCreation
    entity.saveToDatabase()
    return game
}

fun insertParticipant(
    uuid: String = randomGuid(),
    gameId: String = randomGuid(),
    playerId: String = insertPlayer().rowId,
    ordinal: Int = 1,
    finishingPosition: Int = -1,
    finalScore: Int = -1,
    dtFinished: Timestamp = DateStatics.END_OF_TIME,
    teamId: String = "",
    resigned: Boolean = false,
    database: Database = mainDatabase
): ParticipantEntity {
    val pe = ParticipantEntity(database)
    pe.rowId = uuid
    pe.gameId = gameId
    pe.playerId = playerId
    pe.ordinal = ordinal
    pe.finishingPosition = finishingPosition
    pe.finalScore = finalScore
    pe.dtFinished = dtFinished
    pe.teamId = teamId
    pe.resigned = resigned

    pe.saveToDatabase()

    return pe
}

fun insertTeamAndParticipants(
    gameId: String = randomGuid(),
    ordinal: Int = 1,
    finishingPosition: Int = -1,
    finalScore: Int = -1,
    dtFinished: Timestamp = DateStatics.END_OF_TIME,
    playerOne: PlayerEntity = insertPlayer(),
    playerTwo: PlayerEntity = insertPlayer(),
    database: Database = mainDatabase
): TeamEntity {
    val t =
        insertTeam(
            gameId = gameId,
            ordinal = ordinal,
            finishingPosition = finishingPosition,
            finalScore = finalScore,
            dtFinished = dtFinished,
            database = database
        )

    insertParticipant(
        gameId = gameId,
        playerId = playerOne.rowId,
        teamId = t.rowId,
        ordinal = 0,
        database = database
    )

    insertParticipant(
        gameId = gameId,
        playerId = playerTwo.rowId,
        teamId = t.rowId,
        ordinal = 1,
        database = database
    )

    return t
}

fun insertTeam(
    uuid: String = randomGuid(),
    gameId: String = randomGuid(),
    ordinal: Int = 1,
    finishingPosition: Int = -1,
    finalScore: Int = -1,
    dtFinished: Timestamp = DateStatics.END_OF_TIME,
    database: Database = mainDatabase
): TeamEntity {
    val t = TeamEntity(database)
    t.rowId = uuid
    t.gameId = gameId
    t.ordinal = ordinal
    t.finishingPosition = finishingPosition
    t.finalScore = finalScore
    t.dtFinished = dtFinished

    t.saveToDatabase()

    return t
}

fun insertDart(
    participant: ParticipantEntity,
    dart: Dart,
    dtCreation: Timestamp = getSqlDateNow(),
    database: Database = mainDatabase
) {
    insertDart(
        participant,
        randomGuid(),
        dart.roundNumber,
        dart.ordinal,
        dart.startingScore,
        dart.score,
        dart.multiplier,
        dart.segmentType,
        dtCreation = dtCreation,
        database = database
    )
}

fun insertDart(
    participant: ParticipantEntity,
    uuid: String = randomGuid(),
    roundNumber: Int = 1,
    ordinal: Int = 1,
    startingScore: Int = 501,
    score: Int = 20,
    multiplier: Int = 3,
    segmentType: SegmentType = getSegmentTypeForMultiplier(multiplier),
    dtCreation: Timestamp = getSqlDateNow(),
    dtLastUpdate: Timestamp = getSqlDateNow(),
    database: Database = mainDatabase
): DartEntity {
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
    drt.segmentType = segmentType

    drt.saveToDatabase(dtLastUpdate)

    return drt
}

fun getSegmentTypeForMultiplier(multiplier: Int) =
    when (multiplier) {
        1 -> SegmentType.OUTER_SINGLE
        2 -> SegmentType.DOUBLE
        3 -> SegmentType.TREBLE
        else -> SegmentType.MISS
    }

fun List<List<Dart>>.insertIntoDatabase(player: PlayerEntity, participant: ParticipantEntity) {
    val state = makeGolfPlayerState(player, participant)
    forEach { round ->
        round.forEach(state::dartThrown)
        state.commitRound()
    }
}

fun insertDartzeeRoundResult(
    participant: ParticipantEntity = insertParticipant(),
    uuid: String = randomGuid(),
    success: Boolean = true,
    score: Int = 100,
    roundNumber: Int = 2,
    ruleNumber: Int = 2,
    dtCreation: Timestamp = getSqlDateNow(),
    database: Database = mainDatabase
): DartzeeRoundResultEntity {
    val drr = DartzeeRoundResultEntity(database)
    drr.rowId = uuid
    drr.playerId = participant.playerId
    drr.participantId = participant.rowId
    drr.score = score
    drr.success = success
    drr.roundNumber = roundNumber
    drr.ruleNumber = ruleNumber
    drr.dtCreation = dtCreation

    drr.saveToDatabase()
    return drr
}

fun insertGameForReport(
    uuid: String = randomGuid(),
    localId: Long = mainDatabase.generateLocalId(EntityName.Game),
    gameType: GameType = GameType.X01,
    gameParams: String = X01Config(501, FinishType.Doubles).toJson(),
    dtFinish: Timestamp = DateStatics.END_OF_TIME,
    dartsMatchId: String = "",
    matchOrdinal: Int = -1,
    dtCreation: Timestamp = getSqlDateNow(),
    dtLastUpdate: Timestamp = getSqlDateNow()
): GameEntity {
    val game =
        insertGame(
            uuid,
            mainDatabase,
            localId,
            gameType,
            gameParams,
            dtFinish,
            dartsMatchId,
            matchOrdinal,
            dtCreation,
            dtLastUpdate
        )
    val player = insertPlayer()
    insertParticipant(gameId = game.rowId, playerId = player.rowId)

    return game
}

fun insertGame(
    uuid: String = randomGuid(),
    database: Database = mainDatabase,
    localId: Long = database.generateLocalId(EntityName.Game),
    gameType: GameType = GameType.X01,
    gameParams: String = X01Config(501, FinishType.Doubles).toJson(),
    dtFinish: Timestamp = DateStatics.END_OF_TIME,
    dartsMatchId: String = "",
    matchOrdinal: Int = -1,
    dtCreation: Timestamp = getSqlDateNow(),
    dtLastUpdate: Timestamp = getSqlDateNow()
): GameEntity {
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

fun insertDartzeeRule(
    uuid: String = randomGuid(),
    entityName: EntityName = EntityName.DartzeeTemplate,
    entityId: String = "",
    ordinal: Int = 1,
    calculationResult: DartzeeRuleCalculationResult = makeDartzeeRuleCalculationResult(),
    dtCreation: Timestamp = getSqlDateNow(),
    dtLastUpdate: Timestamp = getSqlDateNow()
): DartzeeRuleEntity {
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

fun insertDartzeeTemplate(
    uuid: String = randomGuid(),
    name: String = "Template",
    dtCreation: Timestamp = getSqlDateNow(),
    dtLastUpdate: Timestamp = getSqlDateNow()
): DartzeeTemplateEntity {
    val de = DartzeeTemplateEntity()
    de.rowId = uuid
    de.dtCreation = dtCreation
    de.name = name

    de.saveToDatabase(dtLastUpdate)

    return de
}

fun insertTemplateAndRule(name: String = "Template"): DartzeeTemplateEntity {
    val template = insertDartzeeTemplate(name = name)
    insertDartzeeRule(entityName = EntityName.DartzeeTemplate, entityId = template.rowId)
    insertDartzeeRule(entityName = EntityName.DartzeeTemplate, entityId = template.rowId)
    return template
}

fun insertAchievement(
    uuid: String = randomGuid(),
    playerId: String = randomGuid(),
    type: AchievementType = AchievementType.X01_BEST_FINISH,
    gameIdEarned: String = "",
    achievementCounter: Int = -1,
    achievementDetail: String = "",
    dtAchieved: Timestamp = getSqlDateNow(),
    database: Database = mainDatabase
): AchievementEntity {
    val a = AchievementEntity(database)
    a.rowId = uuid
    a.playerId = playerId
    a.achievementType = type
    a.gameIdEarned = gameIdEarned
    a.achievementCounter = achievementCounter
    a.achievementDetail = achievementDetail
    a.dtAchieved = dtAchieved

    a.saveToDatabase()

    return a
}

fun insertPlayerImage(
    resource: String = "BaboOne",
    database: Database = mainDatabase
): PlayerImageEntity {
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

fun getCountFromTable(table: EntityName, database: Database = mainDatabase) =
    getCountFromTable(table.name, database)

fun getCountFromTable(table: String, database: Database = mainDatabase) =
    database.executeQueryAggregate("SELECT COUNT(1) FROM $table")

/** Retrieve */
fun retrieveGame() = GameEntity().retrieveEntities().maxByOrNull { it.dtLastUpdate }!!

fun retrieveDart() = DartEntity().retrieveEntities().first()

fun retrieveDartsMatch() = DartsMatchEntity().retrieveEntities().first()

fun retrieveParticipant() = ParticipantEntity().retrieveEntities().first()

fun retrieveAchievement() = AchievementEntity().retrieveEntities().first()

fun retrieveX01Finish() = X01FinishEntity().retrieveEntities().first()

fun retrieveDeletionAudit() = DeletionAuditEntity().retrieveEntities().first()

fun retrieveTeam() = TeamEntity().retrieveEntities().first()

fun getAchievementCount(type: AchievementType) =
    mainDatabase.executeQueryAggregate(
        "SELECT COUNT(1) FROM Achievement WHERE AchievementType = '$type'"
    )

fun getAchievementRows(type: AchievementType) =
    AchievementEntity().retrieveEntities("AchievementType = '$type'")

fun retrieveParticipant(gameId: String, playerId: String) =
    ParticipantEntity().retrieveEntities("GameId = '$gameId' AND PlayerId = '$playerId'").first()

data class AchievementSummary(
    val achievementType: AchievementType,
    val achievementCounter: Int,
    val gameIdEarned: String,
    val achievementDetail: String = ""
)

fun retrieveAchievementsForPlayer(playerId: String): List<AchievementSummary> {
    val achievements = AchievementEntity.retrieveAchievements(playerId)
    return achievements.map {
        AchievementSummary(
            it.achievementType,
            it.achievementCounter,
            it.gameIdEarned,
            it.achievementDetail
        )
    }
}

private fun makeInMemoryDatabase(dbName: String = UUID.randomUUID().toString()) =
    Database(dbName = dbName, inMemory = true).also { it.initialiseConnectionPool(1) }

fun usingInMemoryDatabase(
    dbName: String = UUID.randomUUID().toString(),
    withSchema: Boolean = false,
    testBlock: (inMemoryDatabase: Database) -> Unit
) {
    val db = makeInMemoryDatabase(dbName)
    try {
        db.getDirectory().mkdirs()

        if (withSchema) {
            val migrator = DatabaseMigrator(emptyMap())
            migrator.migrateToLatest(db, "Test")
        }

        testBlock(db)
    } finally {
        // Open a test connection so the tidy-up doesn't freak out if we shut it down in the test
        // block
        db.testConnection()

        db.getDirectory().deleteRecursively()
        db.closeConnectionsAndDrop()
    }
}

fun Database.closeConnectionsAndDrop() {
    shutDown()

    try {
        DriverManager.getConnection("${getQualifiedDbName()};drop=true")
    } catch (sqle: SQLException) {
        if (sqle.message != "Database 'memory:$databaseDirectory/$dbName' dropped.") {
            logger.error(LoggingCode("dropInMemoryDatabase"), "Caught: ${sqle.message}", sqle)
        }
    }
}

fun Database.getTableNames(): List<String> {
    val sb = StringBuilder()
    sb.append(" SELECT TableName")
    sb.append(" FROM sys.systables")
    sb.append(" WHERE TableType = 'T'")

    val list = mutableListOf<String>()
    executeQuery(sb).use { rs ->
        while (rs.next()) {
            list.add(rs.getString("TableName"))
        }
    }

    return list.toList()
}

fun runConversion(fromVersion: Int) {
    val conversions = DatabaseMigrations.getConversionsMap().getValue(fromVersion)
    conversions.forEach { it(mainDatabase) }
}
