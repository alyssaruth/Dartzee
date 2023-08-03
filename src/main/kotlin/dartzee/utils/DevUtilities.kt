package dartzee.utils

import dartzee.core.util.DialogUtil
import dartzee.db.DartEntity
import dartzee.db.GameEntity
import dartzee.db.IParticipant
import dartzee.db.ParticipantEntity
import dartzee.db.TeamEntity
import dartzee.db.X01FinishEntity
import dartzee.game.loadParticipants
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
            DialogUtil.showErrorOLD("No games to delete.")
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
            DialogUtil.showErrorOLD("No game exists for ID $localId")
            return
        }

        val scrn = ScreenCache.getDartsGameScreen(gameId)
        if (scrn != null)
        {
            DialogUtil.showErrorOLD("Cannot delete a game that's open.")
            return
        }

        val participants = loadParticipants(gameId)
        val participantEntities: List<IParticipant> = participants.flatMap { it.individuals + it.participant }
        val participantIds = participantEntities.filterIsInstance<ParticipantEntity>().map { it.rowId }
        val teamIds = participantEntities.filterIsInstance<TeamEntity>().map { it.rowId }
        val ptSql = participantIds.getQuotedIdStr()
        val dartCount = if (participantIds.isEmpty()) 0 else DartEntity().countWhere("ParticipantId IN $ptSql")

        val question = """
            Purge all data for Game #$localId? The following rows will be deleted:
            
            Participant: ${participantIds.size} rows
            Team: ${teamIds.size} rows
            Dart: $dartCount rows
        """.trimIndent()

        val answer = DialogUtil.showQuestionOLD(question, false)
        if (answer == JOptionPane.YES_OPTION)
        {
            if (participantIds.isNotEmpty())
            {
                DartEntity().deleteWhere("ParticipantId IN $ptSql")
            }

            TeamEntity().deleteWhere("GameId = '$gameId'")
            ParticipantEntity().deleteWhere("GameId = '$gameId'")
            X01FinishEntity().deleteWhere("GameId = '$gameId'")
            GameEntity().deleteWhere("RowId = '$gameId'")

            DialogUtil.showInfoOLD("Game #$localId has been purged.")
        }
    }
}
