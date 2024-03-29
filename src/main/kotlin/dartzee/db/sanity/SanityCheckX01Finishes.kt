package dartzee.db.sanity

import dartzee.achievements.X01_ROUNDS_TABLE
import dartzee.achievements.ensureX01RoundsTableExists
import dartzee.core.util.TableUtil
import dartzee.utils.InjectedThings.mainDatabase

class SanityCheckX01Finishes : ISanityCheck {
    data class X01Finish(val playerId: String, val gameId: String, val finish: Int)

    override fun runCheck(): List<AbstractSanityCheckResult> {
        ensureX01RoundsTableExists(emptyList(), mainDatabase)

        var sb = StringBuilder()
        sb.append(" SELECT PlayerId, GameId, StartingScore - RemainingScore AS Finish")
        sb.append(" FROM $X01_ROUNDS_TABLE")
        sb.append(" WHERE RemainingScore = 0")
        sb.append(" AND LastDartMultiplier = 2")

        val rawDataFinishes = retrieveX01Finishes(sb)

        sb = StringBuilder()
        sb.append(" SELECT PlayerId, GameId, Finish")
        sb.append(" FROM X01Finish")

        val denormalisedFinishes = retrieveX01Finishes(sb)

        val extra = denormalisedFinishes - rawDataFinishes
        val missing = rawDataFinishes - denormalisedFinishes

        val model = TableUtil.DefaultModel()
        model.addColumn("Status")
        model.addColumn("PlayerId")
        model.addColumn("GameId")
        model.addColumn("Finish")

        missing.forEach { model.addRow(arrayOf("MISSING", it.playerId, it.gameId, it.finish)) }
        extra.forEach { model.addRow(arrayOf("EXTRA", it.playerId, it.gameId, it.finish)) }

        if (model.rowCount > 0) {
            return listOf(SanityCheckResultSimpleTableModel(model, "X01 Finish mismatches"))
        }

        return emptyList()
    }

    private fun retrieveX01Finishes(sb: StringBuilder) =
        mainDatabase.retrieveAsList(sb) { rs ->
            val playerId = rs.getString("PlayerId")
            val gameId = rs.getString("GameId")
            val finish = rs.getInt("Finish")
            X01Finish(playerId, gameId, finish)
        }
}
