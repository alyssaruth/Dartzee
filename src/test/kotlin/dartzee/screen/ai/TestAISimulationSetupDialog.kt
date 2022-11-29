package dartzee.screen.ai

import com.github.alexburlton.swingtest.clickChild
import com.github.alexburlton.swingtest.getChild
import dartzee.ai.AbstractDartsSimulation
import dartzee.ai.AbstractSimulationRunner
import dartzee.ai.DartsSimulationGolf
import dartzee.ai.DartsSimulationX01
import dartzee.core.bean.NumberField
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartsModel
import dartzee.utils.InjectedThings
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import javax.swing.JButton
import javax.swing.JRadioButton

class TestAISimulationSetupDialog: AbstractTest()
{
    @Test
    fun `Should kick off an X01 simulation`()
    {
        val slot = CapturingSlot<AbstractDartsSimulation>()
        val mockRunner = mockk<AbstractSimulationRunner>(relaxed = true)
        every { mockRunner.runSimulation(capture(slot), any(), any()) } just runs

        InjectedThings.simulationRunner = mockRunner

        val model = makeDartsModel()
        val player = insertPlayer(model)
        val dlg = AISimulationSetupDialog(player, model)
        dlg.clickChild<JRadioButton>("501")
        dlg.getChild<NumberField>().value = 15000

        dlg.clickChild<JButton>("Ok")

        val sim = slot.captured
        verify { mockRunner.runSimulation(sim, 15000, false) }

        sim.shouldBeInstanceOf<DartsSimulationX01>()
        sim.model shouldBe model
        sim.player shouldBe player
    }

    @Test
    fun `Should kick off a Golf simulation`()
    {
        val slot = CapturingSlot<AbstractDartsSimulation>()
        val mockRunner = mockk<AbstractSimulationRunner>(relaxed = true)
        every { mockRunner.runSimulation(capture(slot), any(), any()) } just runs

        InjectedThings.simulationRunner = mockRunner

        val model = makeDartsModel()
        val player = insertPlayer(model)
        val dlg = AISimulationSetupDialog(player, model, true)
        dlg.clickChild<JRadioButton>("Golf (18 Holes)")
        dlg.getChild<NumberField>().value = 12000

        dlg.clickChild<JButton>("Ok")

        val sim = slot.captured
        verify { mockRunner.runSimulation(sim, 12000, true) }

        sim.shouldBeInstanceOf<DartsSimulationGolf>()
        sim.model shouldBe model
        sim.player shouldBe player
    }
}