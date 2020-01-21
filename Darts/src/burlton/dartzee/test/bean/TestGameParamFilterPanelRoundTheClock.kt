package burlton.dartzee.test.bean

import burlton.dartzee.test.core.helper.verifyNotCalled
import burlton.dartzee.code.bean.GameParamFilterPanelRoundTheClock
import burlton.dartzee.code.db.CLOCK_TYPE_DOUBLES
import burlton.dartzee.code.db.CLOCK_TYPE_STANDARD
import burlton.dartzee.code.db.CLOCK_TYPE_TREBLES
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.awt.event.ActionListener

class TestGameParamFilterPanelRoundTheClock: AbstractDartsTest()
{
    @Test
    fun `Should select Standard by default`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.rdbtnStandard.isSelected shouldBe true
    }

    @Test
    fun `Should return game params based on the radio selection`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.rdbtnStandard.doClick()
        panel.getGameParams() shouldBe CLOCK_TYPE_STANDARD
        panel.getFilterDesc() shouldBe "Standard games"

        panel.rdbtnDoubles.doClick()
        panel.getGameParams() shouldBe CLOCK_TYPE_DOUBLES
        panel.getFilterDesc() shouldBe "Doubles games"

        panel.rdbtnTrebles.doClick()
        panel.getGameParams() shouldBe CLOCK_TYPE_TREBLES
        panel.getFilterDesc() shouldBe "Trebles games"
    }

    @Test
    fun `Should support setting the selection by gameParams`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.setGameParams(CLOCK_TYPE_DOUBLES)
        panel.rdbtnDoubles.isSelected shouldBe true

        panel.setGameParams(CLOCK_TYPE_TREBLES)
        panel.rdbtnTrebles.isSelected shouldBe true

        panel.setGameParams(CLOCK_TYPE_STANDARD)
        panel.rdbtnStandard.isSelected shouldBe true
    }

    @Test
    fun `Should enable and disable its radio buttons correctly`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        panel.enableChildren(false)
        panel.rdbtnStandard.isEnabled shouldBe false
        panel.rdbtnDoubles.isEnabled shouldBe false
        panel.rdbtnTrebles.isEnabled shouldBe false

        panel.enableChildren(true)
        panel.rdbtnStandard.isEnabled shouldBe true
        panel.rdbtnDoubles.isEnabled shouldBe true
        panel.rdbtnTrebles.isEnabled shouldBe true
    }

    @Test
    fun `Should add and remove action listeners on the radio button panel`()
    {
        val panel = GameParamFilterPanelRoundTheClock()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.rdbtnDoubles.doClick()

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.rdbtnTrebles.doClick()

        verifyNotCalled { listener.actionPerformed(any()) }
    }
}