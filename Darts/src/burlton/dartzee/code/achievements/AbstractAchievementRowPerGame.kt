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
            tm.addColumn("Game")
            tm.addColumn("Date Achieved")

            sortedRows.forEach{
                tm.addRow(arrayOf(it.localGameIdEarned, it.dtLastUpdate))
            }

            tmBreakdown = tm
        }
    }
}