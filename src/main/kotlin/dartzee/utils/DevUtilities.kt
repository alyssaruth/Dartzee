package dartzee.utils

import dartzee.core.util.DialogUtil
import dartzee.db.DartEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.mainDatabase
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
        val gameCount = mainDatabase.executeQueryAggregate("SELECT COUNT(1) FROM Game")
        if (gameCount == 0)
        {
            return null
        }

        val gameIds = mutableListOf<Long>()

        mainDatabase.executeQuery("SELECT LocalId FROM Game").use { rs ->
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

        val participantIds = ParticipantEntity().retrieveEntities("GameId = '$gameId'").map { it.rowId }
        val ptSql = participantIds.joinToString { "'$it'" }
        val dartCount = if (participantIds.isEmpty()) 0 else DartEntity().countWhere("ParticipantId IN ($ptSql)")

        val question = ("Purge all data for Game #$localId? The following rows will be deleted:"
                + "\n\n Participant: ${participantIds.size} rows"
                + "\n Dart: $dartCount rows")

        val answer = DialogUtil.showQuestion(question, false)
        if (answer == JOptionPane.YES_OPTION)
        {
            if (participantIds.isNotEmpty())
            {
                mainDatabase.executeUpdate("DELETE FROM Dart WHERE ParticipantId IN ($ptSql)")
            }

            mainDatabase.executeUpdate("DELETE FROM Participant WHERE GameId = '$gameId'")
            mainDatabase.executeUpdate("DELETE FROM X01Finish WHERE GameId = '$gameId'")
            mainDatabase.executeUpdate("DELETE FROM Game WHERE RowId = '$gameId'")

            DialogUtil.showInfo("Game #$localId has been purged.")
        }
    }
}
