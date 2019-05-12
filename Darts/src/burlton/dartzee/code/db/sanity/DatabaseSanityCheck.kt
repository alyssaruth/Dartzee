package burlton.dartzee.code.db.sanity

import burlton.core.code.util.Debug
import burlton.dartzee.code.`object`.SEGMENT_TYPE_DOUBLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_INNER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_OUTER_SINGLE
import burlton.dartzee.code.`object`.SEGMENT_TYPE_TREBLE
import burlton.dartzee.code.db.*
import burlton.dartzee.code.screen.ScreenCache
import burlton.dartzee.code.utils.DartsDatabaseUtil
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.desktopcore.code.bean.ScrollTableButton
import burlton.desktopcore.code.screen.TableModelDialog
import burlton.desktopcore.code.util.DialogUtil
import burlton.desktopcore.code.util.TableUtil.DefaultModel
import burlton.desktopcore.code.util.getEndOfTimeSqlString
import java.awt.event.ActionEvent
import java.sql.SQLException
import java.util.*
import javax.swing.AbstractAction
import javax.swing.table.DefaultTableModel

object DatabaseSanityCheck
{
    private val sanityErrors = ArrayList<AbstractSanityCheckResult>()

    fun runSanityCheck()
    {
        Debug.appendBanner("RUNNING SANITY CHECK")

        sanityErrors.clear()

        checkForHangingOrUnsetIds()
        checkParticipantTable()
        checkForDuplicateDarts()
        checkForColumnsWithDefaults()
        checkForUnfinishedGames()
        checkFinalScore()
        checkForDuplicateMatchOrdinals()

        //Do this last in case any sanity checks have left rogue tables lying around
        checkForUnexpectedTables()

        val tm = buildResultsModel()
        if (tm.rowCount > 0)
        {
            val showResults = object : AbstractAction()
            {
                override fun actionPerformed(e: ActionEvent)
                {
                    val modelRow = Integer.valueOf(e.actionCommand)

                    val result = sanityErrors[modelRow]
                    showResultsBreakdown(result)
                }
            }

            val autoFix = object : AbstractAction()
            {
                override fun actionPerformed(e: ActionEvent)
                {
                    val modelRow = Integer.valueOf(e.actionCommand)

                    val result = sanityErrors[modelRow]
                    result.autoFix()
                }
            }

            val table = ScrollTableButton(tm)
            table.setButtonColumn(2, showResults)
            table.setButtonColumn(3, autoFix)

            val dlg = TableModelDialog("Sanity Results", table)
            dlg.setColumnWidths("-1;50;150;150")
            dlg.setLocationRelativeTo(ScreenCache.getMainScreen())
            dlg.isVisible = true
        }
        else
        {
            DialogUtil.showInfo("Sanity check completed and found no issues")
        }
    }

    private fun buildResultsModel(): DefaultTableModel
    {
        val model = DefaultModel()
        model.addColumn("Description")
        model.addColumn("Count")
        model.addColumn("")
        model.addColumn("")

        for (result in sanityErrors)
        {
            val row = arrayOf(result.getDescription(), result.getCount(), "View Results >", "Auto-fix")
            model.addRow(row)
        }

        return model
    }

    private fun showResultsBreakdown(result: AbstractSanityCheckResult)
    {
        val dlg = result.getResultsDialog()
        dlg.setSize(800, 600)
        dlg.setLocationRelativeTo(ScreenCache.getMainScreen())
        dlg.isVisible = true
    }

    private fun checkForHangingOrUnsetIds()
    {
        DartsDatabaseUtil.getAllEntities().forEach{
            checkForHangingOrUnsetIds(it)
        }
    }

    private fun checkForHangingOrUnsetIds(entity: AbstractEntity<*>)
    {
        val columns = entity.getColumns()
        val idColumns = columns.filter{ it.endsWith("Id") && it != "RowId" && it != "LocalId" }

        idColumns.forEach{
            checkForHangingValues(entity, it)
            if (!entity.columnCanBeUnset(it))
            {
                checkForUnsetValues(entity, it)
            }
        }
    }

    private fun checkForHangingValues(entity: AbstractEntity<*>, idColumn: String)
    {
        val referencedTable = idColumn.substring(0, idColumn.length - 2)

        val sb = StringBuilder()
        sb.append("$idColumn <> ''")
        sb.append(" AND NOT EXISTS (")
        sb.append(" SELECT 1 FROM $referencedTable ref")
        sb.append(" WHERE e.$idColumn = ref.RowId)")

        val whereSql = sb.toString()
        val entities = entity.retrieveEntities(whereSql, "e")

        val count = entities.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultHangingEntities(idColumn, entities))
        }
    }

    private fun checkForUnsetValues(entity: AbstractEntity<*>, idColumn: String)
    {
        val whereSql = "$idColumn = ''"
        val entities = entity.retrieveEntities(whereSql)

        val count = entities.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultUnsetColumns(idColumn, entities))
        }
    }

    private fun checkParticipantTable()
    {
        val sb = StringBuilder()
        sb.append(" DtFinished < ")
        sb.append(getEndOfTimeSqlString())
        sb.append(" AND FinalScore = -1")

        val whereSql = sb.toString()
        val participants = ParticipantEntity().retrieveEntities(whereSql)

        val count = participants.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultEntitiesSimple(participants, "Participants marked as finished but with no final score"))
        }
    }

    private fun checkForDuplicateDarts()
    {
        val sb = StringBuilder()
        sb.append(" EXISTS (")
        sb.append(" SELECT 1")
        sb.append(" FROM Dart drt2")
        sb.append(" WHERE drt.PlayerId = drt2.PlayerId")
        sb.append(" AND drt.ParticipantId = drt2.ParticipantId")
        sb.append(" AND drt.RoundNumber = drt2.RoundNumber")
        sb.append(" AND drt.Ordinal = drt2.Ordinal")
        sb.append(" AND drt.RowId > drt2.RowId")
        sb.append(")")

        val whereSql = sb.toString()
        val darts = DartEntity().retrieveEntities(whereSql, "drt")
        val count = darts.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultEntitiesSimple(darts, "Duplicate darts"))
        }
    }

    private fun checkForColumnsWithDefaults()
    {
        val sb = StringBuilder()
        sb.append("SELECT t.TableName, c.ColumnName ")
        sb.append("FROM sys.systables t, sys.syscolumns c ")
        sb.append("WHERE c.ReferenceId = t.TableId ")
        sb.append("AND t.TableType = 'T' ")
        sb.append("AND c.ColumnDefault IS NOT NULL")

        val model = DefaultModel()
        model.addColumn("TableName")
        model.addColumn("ColumnName")
        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val tableName = rs.getString("TableName")
                    val columnName = rs.getString("ColumnName")

                    val row = arrayOf(tableName, columnName)
                    model.addRow(row)
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException(sb.toString(), sqle)
        }

        if (model.rowCount > 0)
        {
            sanityErrors.add(SanityCheckResultSimpleTableModel(model, "Columns that allow defaults"))
        }
    }

    /**
     * Primarily to spot temp tables that haven't been tidied up
     */
    private fun checkForUnexpectedTables()
    {
        val entities = DartsDatabaseUtil.getAllEntitiesIncludingVersion()
        val tableNameSql = entities.joinToString{ "'${it.getTableNameUpperCase()}'"}

        val tm = DefaultModel()
        tm.addColumn("Schema")
        tm.addColumn("TableName")
        tm.addColumn("TableId")

        val sb = StringBuilder()
        sb.append(" SELECT s.SchemaName, t.TableName, t.TableId")
        sb.append(" FROM sys.systables t, sys.sysschemas s")
        sb.append(" WHERE t.SchemaId = s.SchemaId")
        sb.append(" AND t.TableType = 'T'")
        sb.append(" AND t.TableName NOT IN ($tableNameSql)")

        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val schema = rs.getString("SchemaName")
                    val tableName = rs.getString("TableName")
                    val tableId = rs.getString("TableId")

                    val row = arrayOf(schema, tableName, tableId)
                    tm.addRow(row)
                }
            }
        }
        catch (sqle: SQLException)
        {
            Debug.logSqlException("" + sb, sqle)
        }

        if (tm.rowCount > 0)
        {
            sanityErrors.add(SanityCheckResultUnexpectedTables(tm))
        }
    }

    /**
     * Look for games where DtFinished = EOT, but all the participants have finished.
     */
    private fun checkForUnfinishedGames()
    {
        val sb = StringBuilder()
        sb.append("DtFinish = ${getEndOfTimeSqlString()}")
        sb.append(" AND NOT EXISTS ")
        sb.append(" (")
        sb.append(" 	SELECT 1")
        sb.append(" 	FROM Participant pt")
        sb.append(" 	WHERE pt.GameId = g.RowId")
        sb.append(" 	AND pt.DtFinished = ${getEndOfTimeSqlString()}")
        sb.append(" )")

        val whereSql = sb.toString()
        val games = GameEntity().retrieveEntities(whereSql, "g")
        if (games.size > 0)
        {
            sanityErrors.add(SanityCheckResultEntitiesSimple(games, "Unfinished games without active players"))
        }
    }

    /**
     * Actually got the potential for this. When loading an unfinished match, the ordinal got reset to 0. Oops...
     */
    private fun checkForDuplicateMatchOrdinals()
    {
        val sb = StringBuilder()
        sb.append(" g.MatchOrdinal > -1")
        sb.append(" AND EXISTS (")
        sb.append(" SELECT 1")
        sb.append(" FROM Game g2")
        sb.append(" WHERE g2.DartsMatchId = g.DartsMatchId")
        sb.append(" AND g2.MatchOrdinal = g.MatchOrdinal")
        sb.append(" AND g2.RowId > g.RowId")
        sb.append(")")

        val whereSql = sb.toString()
        val games = GameEntity().retrieveEntities(whereSql, "g")
        val count = games.size
        if (count > 0)
        {
            sanityErrors.add(SanityCheckResultDuplicateMatchOrdinals(games))
        }
    }

    /**
     * The FinalScore column on Participant is basically denormalised data. Sense-check it against the raw data here.
     */
    private fun checkFinalScore()
    {
        checkFinalScoreX01()
        checkFinalScoreGolf()
        checkFinalScoreRoundTheClock()
    }

    /**
     * Should be (totalRounds - 1) * 3 + (# darts in final round)
     */
    private fun checkFinalScoreX01()
    {
        val tempTable1 = DatabaseUtil.createTempTable("ParticipantToRoundCount", "ParticipantId VARCHAR(36), PlayerId VARCHAR(36), RoundCount INT, FinalRoundNumber INT")
                ?: return

        var sb = StringBuilder()
        sb.append("INSERT INTO $tempTable1")
        sb.append(" SELECT pt.RowId, pt.PlayerId, COUNT(DISTINCT drt.RoundNumber), MAX(drt.RoundNumber)")
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_X01")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" GROUP BY pt.RowId, pt.PlayerId")

        var sql = sb.toString()
        val success = DatabaseUtil.executeUpdate(sql)
        if (!success)
        {
            return
        }

        val tempTable2 = DatabaseUtil.createTempTable("ParticipantToX01Score", "ParticipantId VARCHAR(36), FinalScoreCalculated INT")
                ?: return

        sb = StringBuilder()
        sb.append("INSERT INTO $tempTable2")
        sb.append(" SELECT zz.ParticipantId, 3*(zz.RoundCount - 1) + COUNT(1)")
        sb.append(" FROM Dart drt, $tempTable1 zz")
        sb.append(" WHERE zz.ParticipantId = drt.ParticipantId")
        sb.append(" AND zz.PlayerId = drt.PlayerId")
        sb.append(" AND zz.FinalRoundNumber = drt.RoundNumber")
        sb.append(" GROUP BY zz.ParticipantId, zz.RoundCount")

        sql = sb.toString()

        try
        {
            DatabaseUtil.executeUpdate(sql)
            createFinalScoreSanityCheck(tempTable2, GAME_TYPE_X01)
        }
        finally
        {
            DatabaseUtil.dropTable(tempTable1)
        }
    }

    /**
     *
     */
    private fun checkFinalScoreGolf()
    {
        val tempTable = DatabaseUtil.createTempTable("ParticipantToGolfScore", "ParticipantId VARCHAR(36), FinalScoreCalculated INT")
                ?: return

        val sb = StringBuilder()
        sb.append(" INSERT INTO $tempTable")
        sb.append(" SELECT pt.RowId, SUM(")
        sb.append(" 	CASE")
        sb.append(" 		WHEN drt.Score <> drt.RoundNumber THEN 5")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_DOUBLE THEN 1")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_TREBLE THEN 2")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_INNER_SINGLE THEN 3")
        sb.append(" 		WHEN drt.SegmentType = $SEGMENT_TYPE_OUTER_SINGLE THEN 4")
        sb.append(" 		ELSE 5")
        sb.append(" 	END")
        sb.append(" )")
        sb.append(" FROM Dart drt, Participant pt, Game g")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_GOLF")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND NOT EXISTS")
        sb.append(" (")
        sb.append("     SELECT 1")
        sb.append("     FROM Dart drt2")
        sb.append("     WHERE drt.ParticipantId = drt2.ParticipantId")
        sb.append("     AND drt.PlayerId = drt2.PlayerId")
        sb.append("     AND drt.RoundNumber = drt2.RoundNumber")
        sb.append("     AND drt2.Ordinal > drt.Ordinal")
        sb.append(" )")
        sb.append(" GROUP BY pt.RowID, pt.FinalScore")

        val sql = sb.toString()

        val success = DatabaseUtil.executeUpdate(sql)
        if (!success)
        {
            DatabaseUtil.dropTable(tempTable)
            return
        }

        createFinalScoreSanityCheck(tempTable, GAME_TYPE_GOLF)
    }

    /**
     * Should just equal the number of darts
     */
    private fun checkFinalScoreRoundTheClock()
    {
        val tempTable = DatabaseUtil.createTempTable("ParticipantToRoundTheClockScore", "ParticipantId VARCHAR(36), FinalScoreCalculated INT")
                ?: return

        val sb = StringBuilder()
        sb.append("INSERT INTO $tempTable")
        sb.append(" SELECT pt.RowId, COUNT(1)")
        sb.append(" FROM Game g, Participant pt, Dart drt")
        sb.append(" WHERE drt.ParticipantId = pt.RowId")
        sb.append(" AND drt.PlayerId = pt.PlayerId")
        sb.append(" AND pt.GameId = g.RowId")
        sb.append(" AND g.GameType = $GAME_TYPE_ROUND_THE_CLOCK")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" GROUP BY pt.RowId")

        val sql = sb.toString()
        val success = DatabaseUtil.executeUpdate(sql)
        if (!success)
        {
            return
        }

        createFinalScoreSanityCheck(tempTable, GAME_TYPE_ROUND_THE_CLOCK)
    }

    private fun createFinalScoreSanityCheck(tempTable: String, gameType: Int)
    {
        val sb = StringBuilder()
        sb.append("SELECT pt.*, zz.FinalScoreCalculated")
        sb.append(" FROM Participant pt, $tempTable zz")
        sb.append(" WHERE pt.RowId = zz.ParticipantId")
        sb.append(" AND pt.FinalScore > -1")
        sb.append(" AND pt.FinalScore <> zz.FinalScoreCalculated")

        val hmParticipantToActualCount = mutableMapOf<ParticipantEntity, Int>()
        try
        {
            DatabaseUtil.executeQuery(sb).use { rs ->
                while (rs.next())
                {
                    val pt = ParticipantEntity().factoryFromResultSet(rs)
                    val dartCount = rs.getInt("FinalScoreCalculated")

                    hmParticipantToActualCount[pt] = dartCount
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

        //Add the sanity error
        if (!hmParticipantToActualCount.isEmpty())
        {
            sanityErrors.add(SanityCheckResultFinalScoreMismatch(gameType, hmParticipantToActualCount))
        }
    }
}
