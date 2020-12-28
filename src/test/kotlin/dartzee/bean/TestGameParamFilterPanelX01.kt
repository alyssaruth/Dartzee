package dartzee.bean

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.awt.event.ActionListener

class TestGameParamFilterPanelX01: AbstractTest()
{
    @Test
    fun `Should return game params based on the spinner selection`()
    {
        val panel = GameParamFilterPanelX01()

        panel.spinner.value = 701
        panel.getGameParams() shouldBe "701"
        panel.getFilterDesc() shouldBe "games of 701"

        panel.spinner.value = 301
        panel.getGameParams() shouldBe "301"
        panel.getFilterDesc() shouldBe "games of 301"
    }

    @Test
    fun `Should support setting the selection by gameParams`()
    {
        val panel = GameParamFilterPanelX01()
        panel.setGameParams("701")

        panel.spinner.value shouldBe 701
    }

    @Test
    fun `Should enable and disable its spinner correctly`()
    {
        val panel = GameParamFilterPanelX01()

        panel.enableChildren(false)
        panel.spinner.isEnabled shouldBe false

        panel.enableChildren(true)
        panel.spinner.isEnabled shouldBe true
    }

    @Test
    fun `Should add and remove action listeners on the combo box`()
    {
        val panel = GameParamFilterPanelX01()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.spinner.value = 701

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.spinner.value = 301

        verifyNotCalled { listener.actionPerformed(any()) }
    }
}