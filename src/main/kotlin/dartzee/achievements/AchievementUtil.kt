package dartzee.achievements

import dartzee.achievements.dartzee.AchievementDartzeeGamesWon
import dartzee.achievements.golf.AchievementGolfBestGame
import dartzee.achievements.golf.AchievementGolfCourseMaster
import dartzee.achievements.golf.AchievementGolfGamesWon
import dartzee.achievements.golf.AchievementGolfPointsRisked
import dartzee.achievements.rtc.AchievementClockBestGame
import dartzee.achievements.rtc.AchievementClockBestStreak
import dartzee.achievements.rtc.AchievementClockBruceyBonuses
import dartzee.achievements.rtc.AchievementClockGamesWon
import dartzee.achievements.x01.*
import dartzee.core.screen.ProgressDialog
import dartzee.core.util.Debug
import dartzee.db.AchievementEntity
import dartzee.db.GameType
import dartzee.db.PlayerEntity
import dartzee.utils.DatabaseUtil
import java.sql.SQLException

fun getNotBustSql(): String
{
    val sb = StringBuilder()
    sb.append(" (")
    sb.append("     (drtLast.StartingScore - (drtLast.Score * drtLast.Multiplier) > 1)")
    sb.append("     OR (drtLast.StartingScore - (drtLast.Score * drtLast.Multiplier) = 0 AND drtLast.Multiplier = 2)")
    sb.append(" )")

    return sb.toString()
}

fun getAchievementMaximum() : Int
{
    return getAllAchievements().size * 6
}

fun getPlayerAchievementScore(allAchievementRows: List<AchievementEntity>, player: PlayerEntity): Int
{
    val myAchievementRows = allAchievementRows.filter{it.playerId == player.rowId}

    var score = 0
    for (achievement in getAllAchievements())
    {
        val myRelevantRows = myAchievementRows.filter{it.achievementRef == achievement.achievementRef}.toMutableList()
        achievement.initialiseFromDb(myRelevantRows, player)

        score += achievement.getScore()
    }

    return score
}

fun convertEmptyAchievements()
{
    val emptyAchievements = getAllAchievements().filter{a -> !rowsExistForAchievement(a)}.toMutableList()

    if (!emptyAchievements.isEmpty())
    {
        runConversionsWithProgressBar(emptyAchievements, mutableListOf())
    }
}

fun runConversionsWithProgressBar(achievements: MutableList<AbstractAchievement>, players: List<PlayerEntity>)
{
    val r = Runnable { runConversionsInOtherThread(achievements, players)}
    val t = Thread(r, "Conversion thread")
    t.start()
}

private fun runConversionsInOtherThread(achievements: MutableList<AbstractAchievement>, players: List<PlayerEntity>)
{
    val dlg = ProgressDialog.factory("Populating Achievements", "achievements remaining", achievements.size)
    dlg.setVisibleLater()

    achievements.forEach{
        it.runConversion(players)
        dlg.incrementProgressLater()
    }

    dlg.disposeLater()
}

fun rowsExistForAchievement(achievement: AbstractAchievement) : Boolean
{
    val sql = "SELECT COUNT(1) FROM Achievement WHERE AchievementRef = ${achievement.achievementRef}"
    val count = DatabaseUtil.executeQueryAggregate(sql)

    return count > 0
}

fun getAchievementsForGameType(gameType: GameType) = getAllAchievements().filter{ it.gameType == gameType }

fun getAllAchievements() : MutableList<AbstractAchievement>
{
    return mutableListOf(AchievementX01GamesWon(),
            AchievementGolfGamesWon(),
            AchievementClockGamesWon(),
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
            AchievementDartzeeGamesWon())
}

fun getAchievementForRef(achievementRef : Int) : AbstractAchievement?
{
    for (achievement in getAllAchievements())
    {
        if (achievement.achievementRef == achievementRef)
        {
            return achievement
        }
    }

    Debug.stackTrace("No achievement found for AchievementRef [$achievementRef]")
    return null
}

fun getBestGameAchievement(gameType : GameType) : AbstractAchievementBestGame?
{
    val ref = getAllAchievements().find {it is AbstractAchievementBestGame && it.gameType == gameType}
    return ref as AbstractAchievementBestGame?
}

fun getWinAchievementRef(gameType : GameType) : Int
{
    val ref = getAllAchievements().find {it is AbstractAchievementGamesWon && it.gameType == gameType}?.achievementRef
    if (ref == null)
    {
        Debug.stackTrace("No total wins achievement found for GameType [$gameType]")
    }

    return ref ?: -1
}

fun unlockThreeDartAchievement(playerSql : String, dtColumn: String, lastDartWhereSql: String,
                               achievementScoreSql : String, achievementRef: Int)
{
    val tempTable = DatabaseUtil.createTempTable("PlayerFinishes", "PlayerId VARCHAR(36), GameId VARCHAR(36), DtAchieved TIMESTAMP, Score INT")
            ?: return

    var sb = StringBuilder()
    sb.append("INSERT INTO $tempTable")
    sb.append(" SELECT pt.PlayerId, pt.GameId, $dtColumn, $achievementScoreSql")
    sb.append(" FROM Dart drtFirst, Dart drtLast, Participant pt, Game g")
    sb.append(" WHERE drtFirst.ParticipantId = pt.RowId")
    sb.append(" AND drtFirst.PlayerId = pt.PlayerId")
    sb.append(" AND drtLast.ParticipantId = pt.RowId")
    sb.append(" AND drtLast.PlayerId = pt.PlayerId")
    sb.append(" AND drtFirst.RoundNumber = drtLast.RoundNumber")
    sb.append(" AND drtFirst.Ordinal = 1")
    if (!playerSql.isEmpty())
    {
        sb.append(" AND pt.PlayerId IN ($playerSql)")
    }
    sb.append(" AND $lastDartWhereSql")
    sb.append(" AND pt.GameId = g.RowId")
    sb.append(" AND g.GameType = '${GameType.X01}'")

    if (!DatabaseUtil.executeUpdate("" + sb))
    {
        DatabaseUtil.dropTable(tempTable)
        return
    }

    sb = StringBuilder()
    sb.append(" SELECT PlayerId, GameId, DtAchieved, Score")
    sb.append(" FROM $tempTable zz1")
    sb.append(" WHERE NOT EXISTS (")
    sb.append(" 	SELECT 1")
    sb.append(" 	FROM $tempTable zz2")
    sb.append(" 	WHERE zz2.PlayerId = zz1.PlayerId")
    sb.append(" 	AND (zz2.Score > zz1.Score OR (zz2.Score = zz1.Score AND zz2.DtAchieved < zz1.DtAchieved))")
    sb.append(" )")
    sb.append(" ORDER BY PlayerId")

    try
    {
        DatabaseUtil.executeQuery(sb).use { rs ->
            while (rs.next())
            {
                val playerId = rs.getString("PlayerId")
                val gameId = rs.getString("GameId")
                val dtAchieved = rs.getTimestamp("DtAchieved")
                val score = rs.getInt("Score")

                AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, score, "", dtAchieved)
            }
        }
    }
    catch (sqle: SQLException)
    {
        Debug.logSqlException(sb.toString(), sqle)
    }
    finally
    {
        DatabaseUtil.dropTable(tempTable)
    }
}

fun insertForCheckoutCompleteness(playerId: String, gameId: String, counter: Int)
{
    val achievementRef = ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS
    val whereSql = "PlayerId = '$playerId' AND AchievementRef = $achievementRef"

    val achievementRows = AchievementEntity().retrieveEntities(whereSql)
    val hitDoubles = achievementRows.map { it.achievementCounter }
    if (!hitDoubles.contains(counter))
    {
        AchievementEntity.factoryAndSave(achievementRef, playerId, gameId, counter)

        val template = AchievementX01CheckoutCompleteness()
        val arrayList = ArrayList(hitDoubles)
        arrayList.add(counter)

        template.hitDoubles = arrayList

        AchievementEntity.triggerAchievementUnlock(achievementRows.size, achievementRows.size + 1, template, playerId, gameId)
    }
}

fun retrieveAchievementForDetail(achievementRef: Int, playerId: String, achievementDetail: String): AchievementEntity?
{
    val whereSql = "AchievementRef = $achievementRef AND PlayerId = '$playerId' AND AchievementDetail = '$achievementDetail'"
    return AchievementEntity().retrieveEntity(whereSql)
}