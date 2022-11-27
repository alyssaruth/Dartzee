package dartzee.bean

import com.github.alexburlton.swingtest.doClick
import com.github.alexburlton.swingtest.doHover
import com.github.alexburlton.swingtest.findChild
import com.github.alexburlton.swingtest.shouldMatchImage
import dartzee.achievements.x01.AchievementX01BestThreeDarts
import dartzee.game.GameLauncher
import dartzee.helper.AbstractTest
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementBreakdown
import dartzee.screen.stats.player.PlayerAchievementsScreen
import dartzee.utils.InjectedThings
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.awt.Cursor
import javax.swing.JLabel
import javax.swing.table.DefaultTableModel

class TestAchievementMedal: AbstractTest()
{
    @BeforeEach
    fun beforeEach()
    {
        ScreenCache.get<PlayerAchievementsScreen>().toggleAchievementDesc(false, makeAchievement())
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - locked`()
    {
        val achievement = makeAchievement(-1)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("locked")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - red`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().redThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("red")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - orange`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().orangeThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("orange")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - yellow`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().yellowThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("yellow")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - green`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().greenThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("green")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - blue`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().blueThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("blue")
    }

    @Test
    @Tag("screenshot")
    fun `Should match snapshot - pink`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().pinkThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("pink")
    }

    @Test
    fun `Should update cursor and description for hover if enabled and clickable`()
    {
        val achievement = makeAchievement(30)
        achievement.gameIdEarned = "foo"
        val medal = AchievementMedal(achievement, true)

        medal.doHover(100, 100)
        medal.cursor.type shouldBe Cursor.HAND_CURSOR

        ScreenCache.get<PlayerAchievementsScreen>().findChild<JLabel>("Three Darter").shouldNotBeNull()
    }

    @Test
    fun `Should not update cursor for hover if not clickable`()
    {
        val achievement = makeAchievement(30)
        val medal = AchievementMedal(achievement, true)

        medal.doHover(100, 100)
        medal.cursor.type shouldBe Cursor.DEFAULT_CURSOR
        ScreenCache.get<PlayerAchievementsScreen>().findChild<JLabel>("Three Darter").shouldNotBeNull()
    }

    @Test
    fun `Should not update anything for hover if hover is disabled`()
    {
        val achievement = makeAchievement(30)
        achievement.gameIdEarned = "foo"
        val medal = AchievementMedal(achievement, false)

        medal.doHover(100, 100)
        medal.cursor.type shouldBe Cursor.DEFAULT_CURSOR
        ScreenCache.get<PlayerAchievementsScreen>().findChild<JLabel>("Three Darter") shouldBe null
    }

    @Test
    fun `Should not update for hover if outside the circle`()
    {
        val achievement = makeAchievement(30)
        achievement.gameIdEarned = "foo"
        val medal = AchievementMedal(achievement, true)

        medal.doHover(0, 0)
        medal.cursor.type shouldBe Cursor.DEFAULT_CURSOR
    }

    @Test
    fun `Should show the achievement breakdown on click if it has one`()
    {
        val achievement = makeAchievement(30)
        achievement.tmBreakdown = DefaultTableModel()

        val medal = AchievementMedal(achievement)
        medal.doClick()

        val currentScreen = ScreenCache.currentScreen()
        currentScreen.shouldBeInstanceOf<PlayerAchievementBreakdown>()
        currentScreen.achievement shouldBe achievement
    }

    @Test
    fun `Should launch the relevant game if it has one`()
    {
        val launcher = mockk<GameLauncher>(relaxed = true)
        InjectedThings.gameLauncher = launcher

        val achievement = makeAchievement(30)
        achievement.gameIdEarned = "some-game"

        val medal = AchievementMedal(achievement)
        medal.doClick()

        verify { launcher.loadAndDisplayGame("some-game") }
    }

    private fun makeAchievement(attainedValue: Int = -1) = AchievementX01BestThreeDarts().also { it.attainedValue = attainedValue }
}