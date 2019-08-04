package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.desktopcore.code.util.TableUtil

abstract class AbstractAchievementRowPerGame: AbstractAchievement()
{
    override fun isUnbounded() = true

    override fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?)
    {
        this.player = player

        attainedValue = achievementRows.size

        if (!achievementRows.isEmpty())
        {
            val sortedRows = achievementRows.sortedBy {it.dtLastUpdate}
            val last = sortedRows.last()

            dtLatestUpdate = last.dtLastUpdate

            val tm = TableUtil.DefaultModel()
            tm.setColumnNames(getBreakdownColumns())

            tm.addRows(sortedRows.map{ getBreakdownRow(it) })

            tmBreakdown = tm
        }
    }

    abstract fun getBreakdownColumns(): List<String>
    abstract fun getBreakdownRow(a: AchievementEntity): Array<Any>
}