package dartzee.bean

import io.github.alyssaruth.swingtest.doClick
import dartzee.core.helper.verifyNotCalled
import dartzee.expectErrorDialog
import dartzee.game.GameLauncher
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.utils.InjectedThings
import io.mockk.mockk
import io.mockk.verify
import javax.swing.table.DefaultTableModel
import org.junit.jupiter.api.Test

class ScrollTableDartsGameTest : AbstractTest() {
    @Test
    fun `Should launch a game on click`() {
        val game = insertGame(localId = 4L)
        val launcher = mockk<GameLauncher>(relaxed = true)
        InjectedThings.gameLauncher = launcher

        val tm = DefaultTableModel()
        tm.addColumn("Game")
        tm.addColumn("Thing")
        tm.addRow(arrayOf<Any>(4L, "hiya"))

        val table = ScrollTableDartsGame()
        table.model = tm
        table.table.doClick(async = true)

        verify { launcher.loadAndDisplayGame(game.rowId) }
    }

    @Test
    fun `Should show an error and not launch anything for simulated games`() {
        val launcher = mockk<GameLauncher>(relaxed = true)
        InjectedThings.gameLauncher = launcher

        val tm = DefaultTableModel()
        tm.addColumn("Game")
        tm.addColumn("Thing")
        tm.addRow(arrayOf<Any>(-7L, "hiya"))

        val table = ScrollTableDartsGame()
        table.model = tm
        table.table.doClick(async = true)

        expectErrorDialog("It isn't possible to display individual games from a simulation.")
        verifyNotCalled { launcher.loadAndDisplayGame(any()) }
    }
}
