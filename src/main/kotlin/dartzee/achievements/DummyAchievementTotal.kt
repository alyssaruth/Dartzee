package dartzee.achievements

import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.utils.Database
import dartzee.utils.ResourceCache.URL_ACHIEVEMENT_LOCKED

class DummyAchievementTotal : AbstractAchievement() {
    override val name = "Total Achievements"
    override val desc = ""
    override val achievementType = AchievementType.DUMMY_TOTAL
    override val gameType: GameType? = null
    override val allowedForTeams = false

    override val redThreshold = 1
    override val orangeThreshold = getAchievementMaximum() / 6
    override val yellowThreshold = 2 * getAchievementMaximum() / 6
    override val greenThreshold = 3 * getAchievementMaximum() / 6
    override val blueThreshold = 4 * getAchievementMaximum() / 6
    override val pinkThreshold = 5 * getAchievementMaximum() / 6
    override val maxValue = getAchievementMaximum()

    override fun getIconURL() = URL_ACHIEVEMENT_LOCKED

    override fun populateForConversion(playerIds: List<String>, database: Database) {}

    override fun initialiseFromDb(achievementRows: List<AchievementEntity>, player: PlayerEntity?) {
        this.player = player
        attainedValue = getPlayerAchievementScore(achievementRows, player!!)
    }

    override fun retrieveAllRows() = AchievementEntity().retrieveEntities("")
}
