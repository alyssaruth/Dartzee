package dartzee.bean

import dartzee.`object`.GameLauncher
import dartzee.achievements.x01.AchievementX01BestThreeDarts
import dartzee.doHover
import dartzee.helper.AbstractTest
import dartzee.screen.ScreenCache
import dartzee.screen.stats.player.PlayerAchievementBreakdown
import dartzee.screen.stats.player.PlayerAchievementsScreen
import dartzee.utils.InjectedThings
import doClick
import find
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import shouldMatchImage
import java.awt.Cursor
import javax.swing.JLabel
import javax.swing.table.DefaultTableModel

class TestAchievementMedal: AbstractTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()

        ScreenCache.get<PlayerAchievementsScreen>().toggleAchievementDesc(false, makeAchievement())
    }

    @Test
    fun `Should match snapshot - locked`()
    {
        val achievement = makeAchievement(-1)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("locked")
    }

    @Test
    fun `Should match snapshot - red`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().redThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("red")
    }

    @Test
    fun `Should match snapshot - orange`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().orangeThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("orange")
    }

    @Test
    fun `Should match snapshot - yellow`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().yellowThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("yellow")
    }

    @Test
    fun `Should match snapshot - green`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().greenThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("green")
    }

    @Test
    fun `Should match snapshot - blue`()
    {
        val achievement = makeAchievement(AchievementX01BestThreeDarts().blueThreshold)
        val medal = AchievementMedal(achievement)
        medal.shouldMatchImage("blue")
    }

    @Test
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

        ScreenCache.get<PlayerAchievementsScreen>().find<JLabel>("Three Darter").shouldNotBeNull()
    }

    @Test
    fun `Should not update cursor for hover if not clickable`()
    {
        val achievement = makeAchievement(30)
        val medal = AchievementMedal(achievement, true)

        medal.doHover(100, 100)
        medal.cursor.type shouldBe Cursor.DEFAULT_CURSOR
        ScreenCache.get<PlayerAchievementsScreen>().find<JLabel>("Three Darter").shouldNotBeNull()
    }

    @Test
    fun `Should not update anything for hover if hover is disabled`()
    {
        val achievement = makeAchievement(30)
        achievement.gameIdEarned = "foo"
        val medal = AchievementMedal(achievement, false)

        medal.doHover(100, 100)
        medal.cursor.type shouldBe Cursor.DEFAULT_CURSOR
        ScreenCache.get<PlayerAchievementsScreen>().find<JLabel>("Three Darter") shouldBe null
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
        (currentScreen as PlayerAchievementBreakdown).achievement shouldBe achievement
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