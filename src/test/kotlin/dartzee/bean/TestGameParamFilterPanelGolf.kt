package dartzee.bean

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import java.awt.event.ActionListener
import org.junit.jupiter.api.Test

class TestGameParamFilterPanelGolf : AbstractTest() {
    @Test
    fun `Should select 18 holes by default`() {
        val panel = GameParamFilterPanelGolf()

        panel.rdbtn18.isSelected shouldBe true
    }

    @Test
    fun `Should return game params based on the radio selection`() {
        val panel = GameParamFilterPanelGolf()

        panel.rdbtn9.doClick()
        panel.getGameParams() shouldBe "9"
        panel.getFilterDesc() shouldBe "games of 9 holes"

        panel.rdbtn18.doClick()
        panel.getGameParams() shouldBe "18"
        panel.getFilterDesc() shouldBe "games of 18 holes"
    }

    @Test
    fun `Should support setting the selection by gameParams`() {
        val panel = GameParamFilterPanelGolf()

        panel.setGameParams("9")
        panel.rdbtn9.isSelected shouldBe true

        panel.setGameParams("18")
        panel.rdbtn18.isSelected shouldBe true
    }

    @Test
    fun `Should enable and disable its radio buttons correctly`() {
        val panel = GameParamFilterPanelGolf()

        panel.enableChildren(false)
        panel.rdbtn9.isEnabled shouldBe false
        panel.rdbtn18.isEnabled shouldBe false

        panel.enableChildren(true)
        panel.rdbtn9.isEnabled shouldBe true
        panel.rdbtn18.isEnabled shouldBe true
    }

    @Test
    fun `Should add and remove action listeners on the radio button panel`() {
        val panel = GameParamFilterPanelGolf()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.rdbtn9.doClick()

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.rdbtn18.doClick()

        verifyNotCalled { listener.actionPerformed(any()) }
    }
}
