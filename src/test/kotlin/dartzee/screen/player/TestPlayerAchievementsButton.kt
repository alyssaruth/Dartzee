package dartzee.screen.player

import dartzee.achievements.ACHIEVEMENT_REF_CLOCK_BEST_GAME
import dartzee.achievements.ACHIEVEMENT_REF_GOLF_BEST_GAME
import dartzee.achievements.ACHIEVEMENT_REF_X01_BEST_GAME
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.golf.AchievementGolfBestGame
import dartzee.achievements.rtc.AchievementClockBestGame
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.helper.AbstractTest
import dartzee.helper.insertAchievement
import dartzee.helper.insertPlayer
import io.kotlintest.shouldBe
import org.junit.Test

class TestPlayerAchievementsButton: AbstractTest()
{
    @Test
    fun `Should have the correct text based on the players achievements`()
    {
        //6, 1, 0
        val player = insertPlayer()
        val a1 = insertAchievement(playerId = player.rowId, achievementRef = ACHIEVEMENT_REF_X01_BEST_GAME, achievementCounter = AchievementX01BestGame().pinkThreshold)
        val a2 = insertAchievement(playerId = player.rowId, achievementRef = ACHIEVEMENT_REF_GOLF_BEST_GAME, achievementCounter = AchievementGolfBestGame().redThreshold)
        val a3 = insertAchievement(playerId = player.rowId, achievementRef = ACHIEVEMENT_REF_CLOCK_BEST_GAME, achievementCounter = AchievementClockBestGame().redThreshold + 1)

        val button = PlayerAchievementsButton(player, listOf(a1, a2, a3))

        button.text shouldBe "<html><center><h3>Achievements</h3> 7 / ${getAchievementMaximum()}</center></html>"
    }
}