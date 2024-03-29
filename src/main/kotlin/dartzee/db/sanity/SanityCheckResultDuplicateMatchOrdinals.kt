package dartzee.db.sanity

import dartzee.core.util.DialogUtil
import dartzee.core.util.addUnique
import dartzee.db.AbstractEntity
import dartzee.db.GameEntity
import javax.swing.JOptionPane

/**
 * Check for Games as part of the same match but with the same ordinal. Originally happened due to a
 * bug where the ordinal reset upon re-loading an incomplete match
 */
class SanityCheckResultDuplicateMatchOrdinals(entities: List<AbstractEntity<*>>) :
    AbstractSanityCheckResultEntities(entities) {
    override fun autoFix() {
        val tm = getResultsModel()
        val rowCount = tm.rowCount

        // Get the distinct matches affected
        val matchIds = mutableListOf<String>()
        for (i in 0 until rowCount) {
            val matchId = tm.getValueAt(i, 7).toString()
            matchIds.addUnique(matchId)
        }

        // Just double-check...
        val ans =
            DialogUtil.showQuestionOLD(
                "This will reset the ordinal for all games in ${matchIds.size} matches. Proceed?",
                false
            )
        if (ans == JOptionPane.NO_OPTION) {
            return
        }

        // Fix the matches one at a time
        for (matchId in matchIds) {
            val gameSql = "DartsMatchId = '$matchId' ORDER BY DtCreation"
            val games = GameEntity().retrieveEntities(gameSql)

            games.forEachIndexed { index, gameEntity ->
                gameEntity.matchOrdinal = index + 1
                gameEntity.saveToDatabase()
            }
        }

        DialogUtil.showInfoOLD(
            "Auto-fix complete. You should re-run the sanity check and check there are no errors."
        )
    }

    override fun getDescription() = "Games with duplicate MatchOrdinals"
}
