package dartzee.screen.stats.player

import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.getChild
import dartzee.achievements.AchievementType
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.achievements.x01.AchievementX01HighestBust
import dartzee.bean.AchievementMedal
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Container
import javax.swing.JTabbedPane

class TestPlayerAchievementsScreen
{
    @Test
    fun `Should go back to the desired previous screen`()
    {
        val p = insertPlayer()
        val startingScreen = ScreenCache.currentScreen()

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)
        achievementsScrn.backPressed()

        ScreenCache.currentScreen() shouldBe startingScreen
    }

    @Test
    fun `Should update title with player name and achievement progress`()
    {
        val p = insertPlayer(name = "Bob")
        setUpAchievements(p)

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)
        achievementsScrn.getScreenName() shouldBe "Achievements - Bob - 11/${getAchievementMaximum()}"
    }

    @Test
    fun `Should show achievement details on hover, and clear them when hovered away`()
    {
        val p = insertPlayer()
        val g = insertGame()

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)

        val achievement = AchievementX01BestFinish()
        achievement.attainedValue = 170
        achievement.gameIdEarned = g.rowId
        achievement.localGameIdEarned = g.localId
        achievement.player = p

        achievementsScrn.toggleAchievementDesc(true, achievement)
        achievementsScrn.lblAchievementName.background shouldBe Color.MAGENTA
        achievementsScrn.lblAchievementName.text shouldBe achievement.name
        achievementsScrn.lblAchievementDesc.text shouldBe achievement.desc
        achievementsScrn.lblAchievementExtraDetails.text shouldBe "Earned on 01/01/1900 in Game #${g.localId}"

        achievementsScrn.toggleAchievementDesc(false, achievement)
        achievementsScrn.lblAchievementName.background shouldNotBe Color.MAGENTA
        achievementsScrn.lblAchievementName.text shouldBe ""
        achievementsScrn.lblAchievementDesc.text shouldBe ""
        achievementsScrn.lblAchievementExtraDetails.text shouldBe ""
    }

    @Test
    fun `Should be able to select the right tab per game type`()
    {
        val p = insertPlayer()

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)

        // X01 tab by default
        achievementsScrn.findAchievementMedal(AchievementType.X01_BEST_FINISH) shouldNotBe null
        achievementsScrn.findAchievementMedal(AchievementType.GOLF_BEST_GAME) shouldBe null
        achievementsScrn.findAchievementMedal(AchievementType.DARTZEE_FLAWLESS) shouldBe null

        // Golf
        achievementsScrn.selectTab(GameType.GOLF)
        achievementsScrn.findAchievementMedal(AchievementType.X01_BEST_FINISH) shouldBe null
        achievementsScrn.findAchievementMedal(AchievementType.GOLF_BEST_GAME) shouldNotBe null
        achievementsScrn.findAchievementMedal(AchievementType.DARTZEE_FLAWLESS) shouldBe null

        // Dartzee
        achievementsScrn.selectTab(GameType.DARTZEE)
        achievementsScrn.findAchievementMedal(AchievementType.X01_BEST_FINISH) shouldBe null
        achievementsScrn.findAchievementMedal(AchievementType.GOLF_BEST_GAME) shouldBe null
        achievementsScrn.findAchievementMedal(AchievementType.DARTZEE_FLAWLESS) shouldNotBe null
    }

    @Test
    fun `Should show achievement progress for the right player and right achievement`()
    {
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val g = insertGame()

        AchievementEntity.updateAchievement(AchievementType.X01_BEST_FINISH, p1.rowId, g.rowId, 40)
        AchievementEntity.updateAchievement(AchievementType.X01_HIGHEST_BUST, p1.rowId, g.rowId, 80)
        AchievementEntity.updateAchievement(AchievementType.X01_BEST_FINISH, p2.rowId, g.rowId, 75)

        val p1AchievementScreen = ScreenCache.switchToAchievementsScreen(p1)
        p1AchievementScreen.findAchievementMedal(AchievementType.X01_BEST_FINISH)?.achievement?.attainedValue shouldBe 40
        p1AchievementScreen.findAchievementMedal(AchievementType.X01_HIGHEST_BUST)?.achievement?.attainedValue shouldBe 80

        val p2AchievementScreen = ScreenCache.switchToAchievementsScreen(p2)
        p2AchievementScreen.findAchievementMedal(AchievementType.X01_BEST_FINISH)?.achievement?.attainedValue shouldBe 75
        p2AchievementScreen.findAchievementMedal(AchievementType.X01_HIGHEST_BUST)?.achievement?.isLocked() shouldBe true
    }

    private fun PlayerAchievementsScreen.findAchievementMedal(type: AchievementType) =
        (getChild<JTabbedPane>().selectedComponent as Container).findChild<AchievementMedal> { it.achievement.achievementType == type }

    private fun setUpAchievements(player: PlayerEntity)
    {
        val g = insertGame()

        AchievementEntity.updateAchievement(AchievementType.X01_BEST_FINISH, player.rowId, g.rowId, AchievementX01BestFinish().blueThreshold)
        AchievementEntity.updateAchievement(AchievementType.X01_HIGHEST_BUST, player.rowId, g.rowId, AchievementX01HighestBust().pinkThreshold)
    }
}