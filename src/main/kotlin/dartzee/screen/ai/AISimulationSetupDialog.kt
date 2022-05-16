package dartzee.screen.ai

import dartzee.ai.AbstractDartsSimulation
import dartzee.ai.DartsAiModel
import dartzee.ai.DartsSimulationGolf
import dartzee.ai.DartsSimulationX01
import dartzee.core.bean.NumberField
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.screen.SimpleDialog
import dartzee.db.PlayerEntity
import dartzee.screen.Dartboard
import dartzee.screen.ScreenCache
import dartzee.utils.InjectedThings.simulationRunner
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class AISimulationSetupDialog(private val player: PlayerEntity,
                              private val model: DartsAiModel,
                              private val modal: Boolean = false) : SimpleDialog()
{
    private val panelCenter = JPanel()
    private val lblGameMode = JLabel("Game Mode")
    private val panel_1 = RadioButtonPanel()
    private val rdbtn501 = JRadioButton("501")
    private val rdbtnGolfHoles = JRadioButton("Golf (18 Holes)")
    private val lblNumberOfGames = JLabel("Number of games")
    private val nfNumberOfGames = NumberField(100, 100000)

    init
    {
        title = "Simulation Options"
        setSize(400, 160)
        setLocationRelativeTo(ScreenCache.mainScreen)
        isModal = true

        nfNumberOfGames.columns = 10
        contentPane.add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = MigLayout("", "[][]", "[][]")
        panelCenter.add(lblGameMode, "cell 0 0")
        val flowLayout = panel_1.layout as FlowLayout
        flowLayout.alignment = FlowLayout.LEFT
        panelCenter.add(panel_1, "cell 1 0,grow")
        panel_1.add(rdbtn501)
        panel_1.add(rdbtnGolfHoles)
        panelCenter.add(lblNumberOfGames, "cell 0 1,alignx trailing")
        panelCenter.add(nfNumberOfGames, "cell 1 1,growx")
        nfNumberOfGames.value = 1000
    }

    override fun okPressed()
    {
        val dartboard = Dartboard(100, 100)
        dartboard.simulation = true //Don't do animations etc
        dartboard.paintDartboard()

        val sim = factorySimulationForSelection(dartboard)
        simulationRunner.runSimulation(sim, nfNumberOfGames.getNumber(), modal)
        dispose()
    }

    private fun factorySimulationForSelection(dartboard: Dartboard): AbstractDartsSimulation
    {
        return when (panel_1.selection)
        {
            rdbtn501 -> DartsSimulationX01(dartboard, player, model)
            else -> DartsSimulationGolf(dartboard, player, model)
        }
    }
}
