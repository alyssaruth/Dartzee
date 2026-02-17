package dartzee.screen.stats.player

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldMatch
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.achievements.AchievementType
import dartzee.achievements.dartzee.AchievementDartzeeTeamGamesWon
import dartzee.achievements.getAchievementMaximum
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.achievements.x01.AchievementX01HighestBust
import dartzee.bean.AchievementMedal
import dartzee.db.AchievementEntity
import dartzee.db.PlayerEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertPlayer
import dartzee.screen.ScreenCache
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.awt.Color
import javax.swing.ImageIcon
import javax.swing.JLabel
import org.junit.jupiter.api.Test

class TestPlayerAchievementsScreen : AbstractTest() {
    @Test
    fun `Should go back to the desired previous screen`() {
        val p = insertPlayer()
        val startingScreen = ScreenCache.currentScreen()

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)
        achievementsScrn.backPressed()

        ScreenCache.currentScreen() shouldBe startingScreen
    }

    @Test
    fun `Should update title with player name and achievement progress`() {
        val p = insertPlayer(name = "Bob")
        setUpAchievements(p)

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)
        achievementsScrn.getScreenName() shouldBe
            "Achievements - Bob - 11/${getAchievementMaximum()}"
    }

    @Test
    fun `Should show achievement details on hover, and clear them when hovered away`() {
        val p = insertPlayer()
        val g = insertGame()

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(p)

        val achievement = AchievementX01BestFinish()
        achievement.attainedValue = 170
        achievement.gameIdEarned = g.rowId
        achievement.localGameIdEarned = g.localId
        achievement.player = p

        achievementsScrn.toggleAchievementDesc(true, achievement)
        achievementsScrn.nameLabel().background shouldBe Color.MAGENTA
        achievementsScrn.nameLabel().text shouldBe achievement.name
        achievementsScrn.descriptionLabel().text shouldBe achievement.desc
        achievementsScrn.extraDetailsLabel()!!.text shouldBe
            "Earned on 01/01/1900 in Game #${g.localId}"
        achievementsScrn.individualIndicator().shouldBeVisible()
        achievementsScrn.teamIndicator().shouldBeVisible()

        achievementsScrn.toggleAchievementDesc(false, achievement)
        achievementsScrn.nameLabel().background shouldNotBe Color.MAGENTA
        achievementsScrn.nameLabel().text shouldBe ""
        achievementsScrn.descriptionLabel().text shouldBe ""
        achievementsScrn.extraDetailsLabel()?.shouldBeNull()
        achievementsScrn.individualIndicator().shouldNotBeVisible()
        achievementsScrn.teamIndicator().shouldNotBeVisible()
    }

    @Test
    fun `Should not show description or extra details if achievement is locked`() {
        val achievementsScrn = ScreenCache.switchToAchievementsScreen(insertPlayer())
        achievementsScrn.toggleAchievementDesc(true, AchievementX01BestFinish())

        achievementsScrn.nameLabel().text shouldBe AchievementX01BestFinish().name
        achievementsScrn.descriptionLabel().text shouldBe ""
        achievementsScrn.extraDetailsLabel()!!.text shouldBe ""
    }

    @Test
    fun `Should show correct icons based on whether achievement is available for individuals or teams`() {
        val individualAllowed =
            ImageIcon(javaClass.getResource("/achievements/singlePlayerEnabled.png"))
        val individualNotAllowed =
            ImageIcon(javaClass.getResource("/achievements/singlePlayerDisabled.png"))
        val teamAllowed = ImageIcon(javaClass.getResource("/achievements/multiPlayerEnabled.png"))
        val teamNotAllowed =
            ImageIcon(javaClass.getResource("/achievements/multiPlayerDisabled.png"))

        val achievementsScrn = ScreenCache.switchToAchievementsScreen(insertPlayer())
        achievementsScrn.toggleAchievementDesc(true, AchievementX01BestFinish())
        achievementsScrn.individualIndicator().icon.shouldMatch(individualAllowed)
        achievementsScrn.teamIndicator().icon.shouldMatch(teamAllowed)

        achievementsScrn.toggleAchievementDesc(true, AchievementX01BestGame())
        achievementsScrn.individualIndicator().icon.shouldMatch(individualAllowed)
        achievementsScrn.teamIndicator().icon.shouldMatch(teamNotAllowed)

        achievementsScrn.toggleAchievementDesc(true, AchievementDartzeeTeamGamesWon())
        achievementsScrn.individualIndicator().icon.shouldMatch(individualNotAllowed)
        achievementsScrn.teamIndicator().icon.shouldMatch(teamAllowed)
    }

    @Test
    fun `Should show achievement progress for the right player and right achievement`() {
        val p1 = insertPlayer()
        val p2 = insertPlayer()

        val g = insertGame()

        AchievementEntity.updateAchievement(AchievementType.X01_BEST_FINISH, p1.rowId, g.rowId, 40)
        AchievementEntity.updateAchievement(AchievementType.X01_HIGHEST_BUST, p1.rowId, g.rowId, 80)
        AchievementEntity.updateAchievement(AchievementType.X01_BEST_FINISH, p2.rowId, g.rowId, 75)

        val p1AchievementScreen = ScreenCache.switchToAchievementsScreen(p1)
        p1AchievementScreen
            .findAchievementMedal(AchievementType.X01_BEST_FINISH)
            ?.achievement
            ?.attainedValue shouldBe 40
        p1AchievementScreen
            .findAchievementMedal(AchievementType.X01_HIGHEST_BUST)
            ?.achievement
            ?.attainedValue shouldBe 80

        val p2AchievementScreen = ScreenCache.switchToAchievementsScreen(p2)
        p2AchievementScreen
            .findAchievementMedal(AchievementType.X01_BEST_FINISH)
            ?.achievement
            ?.attainedValue shouldBe 75
        p2AchievementScreen
            .findAchievementMedal(AchievementType.X01_HIGHEST_BUST)
            ?.achievement
            ?.isLocked() shouldBe true
    }

    private fun PlayerAchievementsScreen.nameLabel() = getChild<JLabel>("name")

    private fun PlayerAchievementsScreen.descriptionLabel() = getChild<JLabel>("description")

    private fun PlayerAchievementsScreen.extraDetailsLabel() = findChild<JLabel>("extraDetails")

    private fun PlayerAchievementsScreen.individualIndicator() =
        getChild<JLabel>("individualIndicator")

    private fun PlayerAchievementsScreen.teamIndicator() = getChild<JLabel>("teamIndicator")

    private fun PlayerAchievementsScreen.findAchievementMedal(type: AchievementType) =
        findChild<AchievementMedal> { it.achievement.achievementType == type }

    private fun setUpAchievements(player: PlayerEntity) {
        val g = insertGame()

        AchievementEntity.updateAchievement(
            AchievementType.X01_BEST_FINISH,
            player.rowId,
            g.rowId,
            AchievementX01BestFinish().blueThreshold,
        )
        AchievementEntity.updateAchievement(
            AchievementType.X01_HIGHEST_BUST,
            player.rowId,
            g.rowId,
            AchievementX01HighestBust().pinkThreshold,
        )
    }
}
