package dartzee.screen.ai

import dartzee.ai.AbstractDartsSimulation
import dartzee.ai.DartsAiModel
import dartzee.ai.DartsSimulationGolf
import dartzee.ai.DartsSimulationX01
import dartzee.core.bean.NumberField
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.screen.SimpleDialog
import dartzee.db.PlayerEntity
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.simulationRunner
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import net.miginfocom.swing.MigLayout

class AISimulationSetupDialog(
    private val player: PlayerEntity,
    private val model: DartsAiModel,
    private val modal: Boolean = false
) : SimpleDialog() {
    private val panelCenter = JPanel()
    private val lblGameMode = JLabel("Game Mode")
    private val panelSimulationType = RadioButtonPanel()
    private val rdbtn501 = JRadioButton("501")
    private val rdbtnGolfHoles = JRadioButton("Golf (18 Holes)")
    private val lblNumberOfGames = JLabel("Number of games")
    private val nfNumberOfGames = NumberField(100, 100000)

    init {
        title = "Simulation Options"
        setSize(400, 160)
        setLocationRelativeTo(ScreenCache.mainScreen)
        isModal = InjectedThings.allowModalDialogs

        nfNumberOfGames.columns = 10
        contentPane.add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = MigLayout("", "[][]", "[][]")
        panelCenter.add(lblGameMode, "cell 0 0")
        val flowLayout = panelSimulationType.layout as FlowLayout
        flowLayout.alignment = FlowLayout.LEFT
        panelCenter.add(panelSimulationType, "cell 1 0,grow")
        panelSimulationType.add(rdbtn501)
        panelSimulationType.add(rdbtnGolfHoles)
        panelCenter.add(lblNumberOfGames, "cell 0 1,alignx trailing")
        panelCenter.add(nfNumberOfGames, "cell 1 1,growx")
        nfNumberOfGames.value = 1000
    }

    override fun okPressed() {
        val sim = factorySimulationForSelection()
        simulationRunner.runSimulation(sim, nfNumberOfGames.getNumber(), modal)
        dispose()
    }

    private fun factorySimulationForSelection(): AbstractDartsSimulation {
        return when (panelSimulationType.selection) {
            rdbtn501 -> DartsSimulationX01(player, model)
            else -> DartsSimulationGolf(player, model)
        }
    }
}
