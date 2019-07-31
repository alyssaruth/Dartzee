package burlton.dartzee.code.achievements

import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.code.utils.ResourceCache.URL_ACHIEVEMENT_LOCKED

class DummyAchievementTotal: AbstractAchievement()
{
    override val name = "Total Achievements"
    override val desc = ""
    override val achievementRef = -1
    override val gameType = -1

    override val redThreshold = 1
    override val orangeThreshold = getAchievementMaximum() / 6
    override val yellowThreshold = 2 * getAchievementMaximum() / 6
    override val greenThreshold = 3 * getAchievementMaximum() / 6
    override val blueThreshold = 4 * getAchievementMaximum() / 6
    override val pinkThreshold = 5 * getAchievementMaximum() / 6
    override val maxValue = getAchievementMaximum()

    override fun getIconURL() = URL_ACHIEVEMENT_LOCKED

    override fun populateForConversion(playerIds: String){}

    override fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?)
    {
        this.player = player
        attainedValue = getPlayerAchievementScore(achievementRows, player!!)
    }

    override fun retrieveAllRows() = AchievementEntity().retrieveEntities("")
}