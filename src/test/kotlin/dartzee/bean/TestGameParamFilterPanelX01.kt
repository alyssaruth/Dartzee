package dartzee.bean

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.core.helper.verifyNotCalled
import dartzee.game.FinishType
import dartzee.game.X01Config
import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.verify
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import org.junit.jupiter.api.Test

class TestGameParamFilterPanelX01 : AbstractTest() {
    @Test
    fun `Should return game params based on the selection`() {
        val panel = GameParamFilterPanelX01()

        panel.getChild<SpinnerX01>().value = 701
        panel.getGameParams() shouldBe X01Config(701, FinishType.Doubles).toJson()
        panel.getFilterDesc() shouldBe "games of 701"

        panel.getChild<SpinnerX01>().value = 301
        panel.clickChild<JCheckBox>()
        panel.getGameParams() shouldBe X01Config(301, FinishType.Any).toJson()
        panel.getFilterDesc() shouldBe "games of 301 (relaxed finish)"
    }

    @Test
    fun `Should support setting the selection by gameParams`() {
        val params = X01Config(701, FinishType.Any)

        val panel = GameParamFilterPanelX01()
        panel.setGameParams(params.toJson())

        panel.getChild<SpinnerX01>().value shouldBe 701
        panel.getChild<JCheckBox>().isSelected shouldBe false
    }

    @Test
    fun `Should enable and disable its spinner correctly`() {
        val panel = GameParamFilterPanelX01()

        panel.enableChildren(false)
        panel.getChild<SpinnerX01>().isEnabled shouldBe false
        panel.getChild<JCheckBox>().isEnabled shouldBe false

        panel.enableChildren(true)
        panel.getChild<SpinnerX01>().isEnabled shouldBe true
        panel.getChild<JCheckBox>().isEnabled shouldBe true
    }

    @Test
    fun `Should add and remove action listeners on the combo box`() {
        val panel = GameParamFilterPanelX01()

        val listener = mockk<ActionListener>(relaxed = true)

        panel.addActionListener(listener)
        panel.getChild<SpinnerX01>().value = 701

        verify { listener.actionPerformed(any()) }

        clearAllMocks()

        panel.removeActionListener(listener)
        panel.getChild<SpinnerX01>().value = 301

        verifyNotCalled { listener.actionPerformed(any()) }
    }
}
