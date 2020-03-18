package dartzee.screen.game

import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.core.helper.verifyNotCalled
import dartzee.db.PlayerImageEntity
import dartzee.game.state.DefaultPlayerState
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertPlayer
import dartzee.screen.game.scorer.DartsScorerX01
import dartzee.screen.game.x01.X01MatchScreen
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartsMatchScreen: AbstractTest()
{
    @Test
    fun `Should update appearances on all game tabs`()
    {
        val scrn = setUpMatchScreen()

        val panelOne = mockGameTab()
        val panelTwo = mockGameTab()
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

        val panelOne = mockGameTab()
        val panelTwo = mockGameTab()
        scrn.hmGameIdToTab["foo"] = panelOne
        scrn.hmGameIdToTab["bar"] = panelTwo

        val achievement = AchievementX01BestFinish()
        scrn.achievementUnlocked("bar", "player", achievement)

        verifyNotCalled { panelOne.achievementUnlocked(any(), any()) }
        verify { panelTwo.achievementUnlocked("player", achievement) }
    }

    private fun mockGameTab() = mockk<DartsGamePanel<*, *, DefaultPlayerState<DartsScorerX01>>>(relaxed = true)

    //TODO - finish me

    private fun setUpMatchScreen(): X01MatchScreen
    {
        PlayerImageEntity.createPresets()

        val match = insertDartsMatch()
        return X01MatchScreen(match, mutableListOf(insertPlayer(), insertPlayer()))
    }
}