package dartzee.screen

import dartzee.bean.ComboBoxGameType
import dartzee.bean.GameParamFilterPanelGolf
import dartzee.bean.GameParamFilterPanelX01
import dartzee.core.bean.items
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class TestGameSetupScreen: AbstractTest()
{
    @Test
    fun `Should respond to changing game type`()
    {
        val screen = GameSetupScreen()
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelX01>()

        screen.gameTypeComboBox.updateSelection(GameType.GOLF)
        screen.gameParamFilterPanel.shouldBeInstanceOf<GameParamFilterPanelGolf>()
    }

    private fun ComboBoxGameType.updateSelection(type: GameType)
    {
        selectedItem = items().find { it.hiddenData == type }
    }
}