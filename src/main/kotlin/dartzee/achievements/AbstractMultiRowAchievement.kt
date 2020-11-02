package dartzee.achievements

import dartzee.core.util.TableUtil
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.logging.CODE_MERGE_ROW_SKIPPED
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger

abstract class AbstractMultiRowAchievement: AbstractAchievement()
{
    override fun isUnbounded() = true

    override fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?)
    {
        this.player = player

        attainedValue = calculateAttainedValue(achievementRows)

        if (achievementRows.isNotEmpty())
        {
            val sortedRows = achievementRows.sortedBy { it.dtLastUpdate }
            val last = sortedRows.last()

            dtLatestUpdate = last.dtLastUpdate

            val tm = TableUtil.DefaultModel()
            tm.setColumnNames(getBreakdownColumns())

            tm.addRows(sortedRows.map{ getBreakdownRow(it) })

            tmBreakdown = tm
        }
    }

    override fun mergeIntoOtherDatabase(
        achievementRow: AchievementEntity,
        otherDao: AchievementEntity,
        otherDatabase: Database
    ) {
        val existingRow = otherDao.retrieveEntity("PlayerId = '${achievementRow.playerId}' AND AchievementRef = $achievementRef" +
                " AND AchievementDetail = '${achievementRow.achievementDetail}' AND GameIdEarned = '${achievementRow.gameIdEarned}'")

        if (existingRow == null)
        {
            achievementRow.insertIntoDatabase(otherDatabase)
        }
        else
        {
            logger.info(CODE_MERGE_ROW_SKIPPED, "Not merging achievement row as one already exists",
                "PlayerId" to achievementRow.playerId,
                "AchievementRef" to achievementRow.achievementRef,
                "GameIdEarned" to achievementRow.gameIdEarned,
                "AchievementDetail" to achievementRow.achievementDetail)
        }
    }

    private fun calculateAttainedValue(achievementRows: List<AchievementEntity>) =
        if (useCounter()) achievementRows.sumBy { it.achievementCounter } else achievementRows.size

    open fun useCounter() = false

    abstract fun getBreakdownColumns(): List<String>
    abstract fun getBreakdownRow(a: AchievementEntity): Array<Any>
}