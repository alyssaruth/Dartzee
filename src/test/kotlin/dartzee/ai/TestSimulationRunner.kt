package dartzee.ai

import com.github.alyssaburlton.swingtest.awaitCondition
import com.github.alyssaburlton.swingtest.flushEdt
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.clickCancel
import dartzee.core.helper.verifyNotCalled
import dartzee.core.screen.ProgressDialog
import dartzee.core.util.runOnEventThreadBlocking
import dartzee.db.EntityName
import dartzee.findWindow
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertPlayer
import dartzee.helper.makeDartsModel
import dartzee.helper.makeGameWrapper
import dartzee.logging.CODE_SIMULATION_CANCELLED
import dartzee.logging.CODE_SIMULATION_ERROR
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.LoggingCode
import dartzee.logging.Severity
import dartzee.`object`.DartsClient
import dartzee.screen.stats.player.PlayerStatisticsScreen
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.concurrent.locks.ReentrantLock
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JProgressBar

class TestSimulationRunner : AbstractTest()
{
    @Test
    fun `Should handle an error being thrown from the simulation`()
    {
        val simulation = mockk<AbstractDartsSimulation>(relaxed = true)
        every { simulation.simulateGame(any()) } throws Exception("Something went wrong")

        val runner = SimulationRunner()
        runner.runSimulation(simulation, 1, false)
        waitForSimulation()

        verifyLog(CODE_SIMULATION_ERROR, Severity.ERROR)
        dialogFactory.errorsShown.shouldContainExactly("A serious problem has occurred with the simulation.")
        findResultsWindow() shouldBe null
        findWindow<ProgressDialog>()!!.shouldNotBeVisible()
    }

    @Test
    fun `Should show a progress dialog and allow cancelling`()
    {
        val lock = ReentrantLock(true)
        val blockingSimulation = mockk<AbstractDartsSimulation>(relaxed = true)
        every { blockingSimulation.simulateGame(any()) } answers {
            try {
                lock.lock()
                makeGameWrapper()
            } finally {
                lock.unlock()
            }
        }

        lock.lock()
        awaitCondition { lock.isLocked }

        val runner = SimulationRunner()
        runner.runSimulation(blockingSimulation, 5, false)

        awaitCondition { findWindow<ProgressDialog>() != null }
        flushEdt()
        val progressDialog = findWindow<ProgressDialog>()!!
        progressDialog.shouldBeVisible()
        runOnEventThreadBlocking { progressDialog.clickCancel() }

        lock.unlock()
        waitForSimulation(CODE_SIMULATION_CANCELLED)

        verify { blockingSimulation.simulateGame(-1L) }
        verifyNotCalled { blockingSimulation.simulateGame(-2L) }
        findResultsWindow() shouldBe null
        progressDialog.shouldNotBeVisible()
    }

    @Test
    fun `Should complete simulation and show results in a frame`()
    {
        val model = makeDartsModel()
        val player = insertPlayer(model = model, name = "Alyssa")
        val simulation = DartsSimulationX01(player, model)

        val runner = SimulationRunner()
        runner.runSimulation(simulation, 5, false)
        waitForSimulation()

        val progressDialog = findWindow<ProgressDialog>()!!
        progressDialog.shouldNotBeVisible()
        val progressBar = progressDialog.getChild<JProgressBar>()
        progressBar.maximum shouldBe 5
        progressBar.value shouldBe 5

        findResultsDialog().shouldBeNull()
        val resultsWindow = findResultsWindow()!!
        resultsWindow.isVisible shouldBe true
        resultsWindow.title shouldBe "Simulation Results - Alyssa (5 games)"

        val statsScrn = resultsWindow.getChild<PlayerStatisticsScreen>()
        statsScrn.player shouldBe player
        statsScrn.gameType shouldBe GameType.X01
    }

    @Test
    fun `Should show results in a JDialog if told to be modal`()
    {
        val model = makeDartsModel()
        val player = insertPlayer(model = model, name = "Alyssa")
        val simulation = DartsSimulationX01(player, model)

        val runner = SimulationRunner()
        runner.runSimulation(simulation, 4, true)
        waitForSimulation()

        findResultsWindow().shouldBeNull()
        val resultsWindow = findResultsDialog()!!
        resultsWindow.isVisible shouldBe true
        resultsWindow.title shouldBe "Simulation Results - Alyssa (4 games)"
    }

    @Test
    fun `Should not prompt to save real entities in non-dev mode`()
    {
        val model = makeDartsModel()
        val player = insertPlayer(model = model, name = "Alyssa")
        val simulation = DartsSimulationX01(player, model)

        val runner = SimulationRunner()
        runner.runSimulation(simulation, 1, true)
        waitForSimulation()

        dialogFactory.questionsShown.shouldBeEmpty()
    }

    @Test
    fun `Should not save real entities if response is No`()
    {
        DartsClient.devMode = true
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val model = makeDartsModel()
        val player = insertPlayer(model = model, name = "Alyssa")
        val simulation = DartsSimulationX01(player, model)

        val runner = SimulationRunner()
        runner.runSimulation(simulation, 1, true)
        waitForSimulation()

        dialogFactory.questionsShown.shouldContainExactly("Save real entities?")
        getCountFromTable(EntityName.Game) shouldBe 0
    }

    @Test
    fun `Should save real entities if response is Yes`()
    {
        DartsClient.devMode = true
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val model = makeDartsModel()
        val player = insertPlayer(model = model, name = "Alyssa")
        val simulation = DartsSimulationX01(player, model)

        val runner = SimulationRunner()
        runner.runSimulation(simulation, 3, true)
        waitForSimulation()

        dialogFactory.questionsShown.shouldContainExactly("Save real entities?")
        getCountFromTable(EntityName.Game) shouldBe 3
        getCountFromTable(EntityName.Participant) shouldBe 3
    }

    private fun findResultsWindow() = findWindow<JFrame> { it.title.contains("Simulation Results") }
    private fun findResultsDialog() = findWindow<JDialog> { it.title.contains("Simulation Results") }

    private fun waitForSimulation(loggingCode: LoggingCode = CODE_SIMULATION_FINISHED) {
        awaitCondition { findLog(loggingCode) != null }
        flushEdt()
    }
}