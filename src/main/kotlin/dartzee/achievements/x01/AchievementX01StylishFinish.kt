package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievement
import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.game.GameType
import dartzee.utils.Database
import java.net.URL

class AchievementX01StylishFinish : AbstractMultiRowAchievement()
{
    override val name = "Stylish Finish"
    override val desc = "Finishes that involved hitting another double or treble"
    override val achievementType = AchievementType.X01_STYLISH_FINISH
    override val redThreshold = 1
    override val orangeThreshold = 5
    override val yellowThreshold = 10
    override val greenThreshold = 25
    override val blueThreshold = 50
    override val pinkThreshold = 100
    override val maxValue = 100
    override val gameType = GameType.X01
    override val allowedForTeams = true

    override fun getBreakdownColumns() = listOf("Finish", "Method", "Game", "Date Achieved")
    override fun getBreakdownRow(a: AchievementEntity) = arrayOf<Any>(a.achievementCounter, a.achievementDetail, a.localGameIdEarned, a.dtAchieved)

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        TODO("Not yet implemented")
    }

    override fun getIconURL(): URL? {
        TODO("Not yet implemented")
    }

}