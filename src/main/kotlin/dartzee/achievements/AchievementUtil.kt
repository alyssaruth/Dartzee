package dartzee.achievements

import dartzee.achievements.dartzee.AchievementDartzeeBestGame
import dartzee.achievements.dartzee.AchievementDartzeeBingo
import dartzee.achievements.dartzee.AchievementDartzeeFlawless
import dartzee.achievements.dartzee.AchievementDartzeeGamesWon
import dartzee.achievements.dartzee.AchievementDartzeeHalved
import dartzee.achievements.dartzee.AchievementDartzeeTeamGamesWon
import dartzee.achievements.dartzee.AchievementDartzeeUnderPressure
import dartzee.achievements.golf.AchievementGolfBestGame
import dartzee.achievements.golf.AchievementGolfCourseMaster
import dartzee.achievements.golf.AchievementGolfGamesWon
import dartzee.achievements.golf.AchievementGolfInBounds
import dartzee.achievements.golf.AchievementGolfOneHitWonder
import dartzee.achievements.golf.AchievementGolfPointsRisked
import dartzee.achievements.golf.AchievementGolfTeamGamesWon
import dartzee.achievements.rtc.AchievementClockBestGame
import dartzee.achievements.rtc.AchievementClockBestStreak
import dartzee.achievements.rtc.AchievementClockBruceyBonuses
import dartzee.achievements.rtc.AchievementClockGamesWon
import dartzee.achievements.rtc.AchievementClockTeamGamesWon
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.achievements.x01.AchievementX01BestThreeDarts
import dartzee.achievements.x01.AchievementX01Btbf
import dartzee.achievements.x01.AchievementX01CheckoutCompleteness
import dartzee.achievements.x01.AchievementX01Chucklevision
import dartzee.achievements.x01.AchievementX01GamesWon
import dartzee.achievements.x01.AchievementX01HighestBust
import dartzee.achievements.x01.AchievementX01HotelInspector
import dartzee.achievements.x01.AchievementX01NoMercy
import dartzee.achievements.x01.AchievementX01Shanghai
import dartzee.achievements.x01.AchievementX01StylishFinish
import dartzee.achievements.x01.AchievementX01SuchBadLuck
import dartzee.achievements.x01.AchievementX01TeamGamesWon
import dartzee.core.screen.ProgressDialog
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.logging.CODE_ACHIEVEMENT_CONVERSION_FINISHED
import dartzee.logging.CODE_ACHIEVEMENT_CONVERSION_STARTED
import dartzee.logging.KEY_ACHIEVEMENT_TIMINGS
import dartzee.logging.KEY_ACHIEVEMENT_TYPES
import dartzee.logging.KEY_PLAYER_IDS
import dartzee.utils.DartsColour
import dartzee.utils.Database
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.ResourceCache
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.net.URL

const val MAX_ACHIEVEMENT_SCORE = 6

fun getAchievementMaximum() = getAllAchievements().size * MAX_ACHIEVEMENT_SCORE

fun getPlayerAchievementScore(
    allAchievementRows: List<AchievementEntity>,
    player: PlayerEntity
): Int {
    val myAchievementRows = allAchievementRows.filter { it.playerId == player.rowId }

    return getAllAchievements().sumOf { achievement ->
        val myRelevantRows =
            myAchievementRows.filter { it.achievementType == achievement.achievementType }
        achievement.initialiseFromDb(myRelevantRows, player)
        achievement.getScore()
    }
}

fun convertEmptyAchievements() {
    val emptyAchievements = getAllAchievements().filter { !rowsExistForAchievement(it) }
    if (emptyAchievements.isNotEmpty()) {
        runConversionsWithProgressBar(emptyAchievements, mutableListOf())
    }
}

fun runConversionsWithProgressBar(
    achievements: List<AbstractAchievement>,
    playerIds: List<String>,
    database: Database = mainDatabase
): Thread {
    val r = Runnable { runConversionsInOtherThread(achievements, playerIds, database) }
    val t = Thread(r, "Conversion thread")
    t.start()
    return t
}

private fun runConversionsInOtherThread(
    achievements: List<AbstractAchievement>,
    playerIds: List<String>,
    database: Database
) {
    val dlg =
        ProgressDialog.factory(
            "Populating Achievements",
            "achievements remaining",
            achievements.size
        )
    dlg.setVisibleLater()

    val playerCount = if (playerIds.isEmpty()) "all" else "${playerIds.size}"
    logger.info(
        CODE_ACHIEVEMENT_CONVERSION_STARTED,
        "Regenerating ${achievements.size} achievements for $playerCount players",
        KEY_PLAYER_IDS to playerIds,
        KEY_ACHIEVEMENT_TYPES to achievements.map { it.achievementType }
    )

    val timings = mutableMapOf<String, Long>()

    try {
        database.dropUnexpectedTables()

        achievements.forEach { achievement ->
            val timer = DurationTimer()
            achievement.runConversion(playerIds, database)

            val timeElapsed = timer.getDuration()
            timings[achievement.name] = timeElapsed

            dlg.incrementProgressLater()
        }
    } finally {
        database.dropUnexpectedTables()
    }

    val totalTime = timings.values.sum()
    logger.info(
        CODE_ACHIEVEMENT_CONVERSION_FINISHED,
        "Done in $totalTime",
        KEY_ACHIEVEMENT_TIMINGS to timings
    )

    dlg.disposeLater()
}

private fun rowsExistForAchievement(achievement: AbstractAchievement): Boolean {
    val sql =
        "SELECT COUNT(1) FROM Achievement WHERE AchievementType = '${achievement.achievementType}'"
    val count = mainDatabase.executeQueryAggregate(sql)

    return count > 0
}

fun getAchievementsForGameType(gameType: GameType) =
    getAllAchievements().filter { it.gameType == gameType }

fun getAllAchievements() =
    listOf(
        AchievementX01GamesWon(),
        AchievementGolfGamesWon(),
        AchievementClockGamesWon(),
        AchievementX01TeamGamesWon(),
        AchievementGolfTeamGamesWon(),
        AchievementClockTeamGamesWon(),
        AchievementX01BestGame(),
        AchievementGolfBestGame(),
        AchievementClockBestGame(),
        AchievementX01BestFinish(),
        AchievementX01BestThreeDarts(),
        AchievementX01CheckoutCompleteness(),
        AchievementX01HighestBust(),
        AchievementGolfPointsRisked(),
        AchievementClockBruceyBonuses(),
        AchievementX01Shanghai(),
        AchievementX01HotelInspector(),
        AchievementX01SuchBadLuck(),
        AchievementX01Btbf(),
        AchievementClockBestStreak(),
        AchievementX01NoMercy(),
        AchievementGolfCourseMaster(),
        AchievementDartzeeGamesWon(),
        AchievementDartzeeTeamGamesWon(),
        AchievementDartzeeBestGame(),
        AchievementDartzeeFlawless(),
        AchievementDartzeeUnderPressure(),
        AchievementDartzeeBingo(),
        AchievementDartzeeHalved(),
        AchievementX01Chucklevision(),
        AchievementGolfOneHitWonder(),
        AchievementGolfInBounds(),
        AchievementX01StylishFinish()
    )

fun getAchievementForType(achievementType: AchievementType) =
    getAllAchievements().find { it.achievementType == achievementType }

fun getBestGameAchievement(gameType: GameType): AbstractAchievementBestGame? {
    val ref =
        getAllAchievements().find { it is AbstractAchievementBestGame && it.gameType == gameType }
    return ref as AbstractAchievementBestGame?
}

fun getWinAchievementType(gameType: GameType) =
    getAllAchievements()
        .first { it is AbstractAchievementGamesWon && it.gameType == gameType }
        .achievementType

fun getTeamWinAchievementType(gameType: GameType) =
    getAllAchievements()
        .first { it is AbstractAchievementTeamGamesWon && it.gameType == gameType }
        .achievementType

fun unlockThreeDartAchievement(
    playerIds: List<String>,
    x01RoundWhereSql: String,
    achievementScoreSql: String,
    achievementType: AchievementType,
    database: Database
) {
    ensureX01RoundsTableExists(playerIds, database)

    val tempTable =
        database.createTempTable(
            "PlayerResults",
            "PlayerId VARCHAR(36), GameId VARCHAR(36), DtAchieved TIMESTAMP, Score INT"
        )

    var sb = StringBuilder()
    sb.append(" INSERT INTO $tempTable")
    sb.append(" SELECT PlayerId, GameId, DtRoundFinished, $achievementScoreSql")
    sb.append(" FROM $X01_ROUNDS_TABLE")
    sb.append(" WHERE $x01RoundWhereSql")

    if (!database.executeUpdate(sb)) return

    val zzPlayerToScore =
        database.createTempTable("PlayerToThreeDartScore", "PlayerId VARCHAR(36), Score INT")

    sb = StringBuilder()
    sb.append(" INSERT INTO $zzPlayerToScore")
    sb.append(" SELECT PlayerId, MAX(Score)")
    sb.append(" FROM $tempTable")
    sb.append(" GROUP BY PlayerId")

    if (!database.executeUpdate(sb)) return

    sb = StringBuilder()
    sb.append(" SELECT rslt.*")
    sb.append(" FROM $tempTable rslt, $zzPlayerToScore zz")
    sb.append(" WHERE rslt.PlayerId = zz.PlayerId")
    sb.append(" AND rslt.Score = zz.Score")
    sb.append(" ORDER BY DtAchieved")

    database.executeQuery(sb).use { rs ->
        bulkInsertFromResultSet(
            rs,
            database,
            achievementType,
            oneRowPerPlayer = true,
            achievementCounterFn = { rs.getInt("Score") }
        )
    }
}

fun retrieveAchievementForDetail(
    achievementType: AchievementType,
    playerId: String,
    achievementDetail: String
): AchievementEntity? {
    val whereSql =
        "AchievementType = '$achievementType' AND PlayerId = '$playerId' AND AchievementDetail = '$achievementDetail'"
    return AchievementEntity().retrieveEntity(whereSql)
}

fun getGamesWonIcon(gameType: GameType): URL? =
    when (gameType) {
        GameType.X01 -> ResourceCache.URL_ACHIEVEMENT_X01_GAMES_WON
        GameType.GOLF -> ResourceCache.URL_ACHIEVEMENT_GOLF_GAMES_WON
        GameType.ROUND_THE_CLOCK -> ResourceCache.URL_ACHIEVEMENT_CLOCK_GAMES_WON
        GameType.DARTZEE -> ResourceCache.URL_ACHIEVEMENT_DARTZEE_GAMES_WON
    }

fun paintMedalCommon(
    g: Graphics2D,
    achievement: AbstractAchievement,
    size: Int,
    highlighted: Boolean
) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    // Draw the track
    g.color = Color.DARK_GRAY.brighter()
    g.fillArc(0, 0, size, size, 0, 360)

    // Mark the levels
    markThreshold(g, achievement, size, Color.MAGENTA, achievement.pinkThreshold)
    markThreshold(g, achievement, size, Color.CYAN, achievement.blueThreshold)
    markThreshold(g, achievement, size, Color.GREEN, achievement.greenThreshold)
    markThreshold(g, achievement, size, Color.YELLOW, achievement.yellowThreshold)
    markThreshold(
        g,
        achievement,
        size,
        DartsColour.COLOUR_ACHIEVEMENT_ORANGE,
        achievement.orangeThreshold
    )
    markThreshold(g, achievement, size, Color.RED, achievement.redThreshold)

    // Draw the actual progress
    val angle = achievement.getAngle()
    g.color = achievement.getColor(highlighted).darker()
    g.fillArc(0, 0, size, size, 90, -angle.toInt())

    // Inner circle
    g.color = achievement.getColor(highlighted)
    g.fillArc(15, 15, size - 30, size - 30, 0, 360)
}

private fun markThreshold(
    g: Graphics2D,
    achievement: AbstractAchievement,
    size: Int,
    color: Color,
    threshold: Int
) {
    g.color = color
    val thresholdAngle = achievement.getAngle(threshold)
    g.fillArc(0, 0, size, size, 90 - thresholdAngle.toInt(), 3)
}
