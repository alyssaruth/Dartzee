package dartzee.utils

import dartzee.core.util.DialogUtil
import dartzee.db.DartEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.screen.ScreenCache
import javax.swing.JOptionPane

object DevUtilities
{
    fun purgeGame()
    {
        val gameIds = getAllGameIds()
        if (gameIds == null)
        {
            DialogUtil.showError("No games to delete.")
            return
        }

        val choice = DialogUtil.showInput("Delete Game", "Select Game ID", gameIds, gameIds[0]) ?: return
        purgeGame(choice)
    }
    private fun getAllGameIds(): Array<Long>?
    {
        val gameCount = DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM Game")
        if (gameCount == 0)
        {
            return null
        }

        val gameIds = mutableListOf<Long>()

        DatabaseUtil.executeQuery("SELECT LocalId FROM Game").use { rs ->
            while (rs.next())
            {
                val rowId = rs.getLong("LocalId")
                gameIds.add(rowId)
            }
        }

        return gameIds.toTypedArray()
    }

    fun purgeGame(localId: Long)
    {
        val gameId = GameEntity.getGameId(localId)
        if (gameId == null)
        {
            DialogUtil.showError("No game exists for ID $localId")
            return
        }

        val scrn = ScreenCache.getDartsGameScreen(gameId)
        if (scrn != null)
        {
            DialogUtil.showError("Cannot delete a game that's open.")
            return
        }

        val participants = ParticipantEntity().retrieveEntities("GameId = '$gameId'")
        val darts = DartEntity().retrieveEntitiesWithFrom(getDartFromSql(gameId), "d")

        val question = ("Purge all data for Game #$localId? The following rows will be deleted:"
                + "\n\n Participant: ${participants.size} rows"
                + "\n Dart: ${darts.size} rows")

        val answer = DialogUtil.showQuestion(question, false)
        if (answer == JOptionPane.YES_OPTION)
        {
            darts.forEach{ it.deleteFromDatabase() }
            participants.forEach{ it.deleteFromDatabase() }

            val gameDeleteSql = "DELETE FROM Game WHERE RowId = '$gameId'"
            DatabaseUtil.executeUpdate(gameDeleteSql)

            DialogUtil.showInfo("Game #$localId has been purged.")
        }
    }

    private fun getDartFromSql(gameId: String): String
    {
        return ("FROM Dart d "
                + "INNER JOIN Participant p ON (d.ParticipantId = p.RowId AND d.PlayerId = p.PlayerId AND p.GameId = '" + gameId + "')")
    }
}
