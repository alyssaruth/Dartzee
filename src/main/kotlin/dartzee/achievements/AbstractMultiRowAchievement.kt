package dartzee.achievements

import dartzee.core.util.TableUtil
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity

abstract class AbstractMultiRowAchievement: AbstractAchievement()
{
    override fun isUnbounded() = true

    override fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?)
    {
        this.player = player

        attainedValue = calculateAttainedValue(achievementRows)

        if (achievementRows.isNotEmpty())
        {
            val sortedRows = achievementRows.sortedBy { it.dtAchieved }
            val last = sortedRows.last()

            dtLatestUpdate = last.dtAchieved

            val tm = TableUtil.DefaultModel()
            tm.setColumnNames(getBreakdownColumns())

            tm.addRows(sortedRows.map{ getBreakdownRow(it) })

            tmBreakdown = tm
        }
    }

    private fun calculateAttainedValue(achievementRows: List<AchievementEntity>) =
        if (useCounter()) achievementRows.sumBy { it.achievementCounter } else achievementRows.size

    open fun useCounter() = false

    abstract fun getBreakdownColumns(): List<String>
    abstract fun getBreakdownRow(a: AchievementEntity): Array<Any>
}