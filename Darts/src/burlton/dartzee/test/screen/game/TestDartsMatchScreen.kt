package burlton.dartzee.test.screen.game

import burlton.dartzee.test.core.helper.verifyNotCalled
import burlton.dartzee.code.achievements.x01.AchievementX01BestFinish
import burlton.dartzee.code.db.PlayerImageEntity
import burlton.dartzee.code.screen.game.DartsGamePanel
import burlton.dartzee.code.screen.game.DartsMatchScreen
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertDartsMatch
import burlton.dartzee.test.helper.insertPlayer
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartsMatchScreen: AbstractDartsTest()
{
    @Test
    fun `Should update appearances on all game tabs`()
    {
        val scrn = setUpMatchScreen()

        val panelOne = mockk<DartsGamePanel<*, *>>(relaxed = true)
        val panelTwo = mockk<DartsGamePanel<*, *>>(relaxed = true)
        scrn.hmGameIdToTab["foo"] = panelOne
        scrn.hmGameIdToTab["bar"] = panelTwo

        scrn.fireAppearancePreferencesChanged()

        verify { panelOne.fireAppearancePreferencesChanged() }
        verify { panelTwo.fireAppearancePreferencesChanged() }
    }

    @Test
    fun `Should pop up achievement unlocks on the correct tab`()
    {
        val scrn = setUpMatchScreen()

        val panelOne = mockk<DartsGamePanel<*, *>>(relaxed = true)
        val panelTwo = mockk<DartsGamePanel<*, *>>(relaxed = true)
        scrn.hmGameIdToTab["foo"] = panelOne
        scrn.hmGameIdToTab["bar"] = panelTwo

        val achievement = AchievementX01BestFinish()
        scrn.achievementUnlocked("bar", "player", achievement)

        verifyNotCalled { panelOne.achievementUnlocked(any(), any()) }
        verify { panelTwo.achievementUnlocked("player", achievement) }
    }

    //TODO - finish me

    private fun setUpMatchScreen(): DartsMatchScreen
    {
        PlayerImageEntity.createPresets()

        val match = insertDartsMatch()
        return DartsMatchScreen(match, mutableListOf(insertPlayer(), insertPlayer()))
    }
}