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
    override val orangeThreshold: Int
        get() = TODO("Not yet implemented")
    override val yellowThreshold: Int
        get() = TODO("Not yet implemented")
    override val greenThreshold: Int
        get() = TODO("Not yet implemented")
    override val blueThreshold: Int
        get() = TODO("Not yet implemented")
    override val pinkThreshold: Int
        get() = TODO("Not yet implemented")
    override val maxValue: Int
        get() = TODO("Not yet implemented")
    override val gameType: GameType?
        get() = TODO("Not yet implemented")
    override val allowedForTeams: Boolean
        get() = TODO("Not yet implemented")

    override fun getBreakdownColumns(): List<String> {
        TODO("Not yet implemented")
    }

    override fun getBreakdownRow(a: AchievementEntity): Array<Any> {
        TODO("Not yet implemented")
    }

    override fun populateForConversion(playerIds: List<String>, database: Database) {
        TODO("Not yet implemented")
    }

    override fun getIconURL(): URL? {
        TODO("Not yet implemented")
    }

}