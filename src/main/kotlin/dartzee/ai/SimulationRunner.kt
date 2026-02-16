package dartzee.ai

import dartzee.core.screen.ProgressDialog
import dartzee.core.util.DialogUtil
import dartzee.db.BulkInserter
import dartzee.db.DartEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.logging.CODE_SIMULATION_CANCELLED
import dartzee.logging.CODE_SIMULATION_ERROR
import dartzee.logging.CODE_SIMULATION_FINISHED
import dartzee.logging.CODE_SIMULATION_PROGRESS
import dartzee.logging.CODE_SIMULATION_STARTED
import dartzee.`object`.DartsClient
import dartzee.screen.stats.player.PlayerStatisticsScreen
import dartzee.stats.GameWrapper
import dartzee.utils.DurationTimer
import dartzee.utils.InjectedThings
import java.awt.BorderLayout
import java.awt.Window
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class SimulationRunner {
    fun runSimulation(sim: AbstractDartsSimulation, numberOfGames: Int, modal: Boolean) {
        val simulationRunnable = Runnable { runSimulationThreadily(sim, numberOfGames, modal) }

        val t = Thread(simulationRunnable, "Simulation Thread")
        t.start()
    }

    private fun runSimulationThreadily(
        sim: AbstractDartsSimulation,
        numberOfGames: Int,
        modal: Boolean,
    ) {
        val dialog = ProgressDialog.factory("Simulating games...", "games remaining", numberOfGames)
        dialog.showCancel(true)
        dialog.setVisibleLater()

        InjectedThings.logger.info(
            CODE_SIMULATION_STARTED,
            "Starting simulation for $numberOfGames games",
        )
        val timer = DurationTimer()

        val hmGameIdToWrapper = mutableMapOf<Long, GameWrapper>()
        for (i in 1..numberOfGames) {
            try {
                val wrapper = sim.simulateGame((-i).toLong())
                hmGameIdToWrapper[-i.toLong()] = wrapper
                dialog.incrementProgressLater()

                InjectedThings.logger.logProgress(
                    CODE_SIMULATION_PROGRESS,
                    i.toLong(),
                    numberOfGames.toLong(),
                )

                if (dialog.cancelPressed()) {
                    InjectedThings.logger.info(CODE_SIMULATION_CANCELLED, "Simulation Cancelled")
                    hmGameIdToWrapper.clear()
                    dialog.disposeLater()
                    return
                }
            } catch (t: Throwable) {
                hmGameIdToWrapper.clear()
                InjectedThings.logger.error(
                    CODE_SIMULATION_ERROR,
                    "Caught $t running simulation",
                    t,
                )
                DialogUtil.showErrorLater("A serious problem has occurred with the simulation.")
            }
        }

        InjectedThings.logger.info(
            CODE_SIMULATION_FINISHED,
            "Simulation completed in ${timer.getDuration()} millis",
        )
        dialog.disposeLater()

        if (hmGameIdToWrapper.isNotEmpty()) {
            // The simulation finished successfully, so show it
            SwingUtilities.invokeLater { simulationFinished(hmGameIdToWrapper, sim, modal) }
        }
    }

    private fun simulationFinished(
        hmGameIdToWrapper: Map<Long, GameWrapper>,
        sim: AbstractDartsSimulation,
        modal: Boolean,
    ) {
        if (DartsClient.devMode) {
            val ans = DialogUtil.showQuestionOLD("Save real entities?")
            handleSavingEntities(ans == JOptionPane.YES_OPTION, hmGameIdToWrapper)
        }

        val title = "Simulation Results - ${sim.player.name} (${hmGameIdToWrapper.size} games)"
        val parentWindow = getParentWindowForResults(title, modal)
        parentWindow.setSize(1200, 800)
        parentWindow.layout = BorderLayout(0, 0)

        val scrn = PlayerStatisticsScreen()
        scrn.setVariables(sim.gameType, sim.player)
        scrn.initFake(hmGameIdToWrapper)
        parentWindow.add(scrn)
        parentWindow.isVisible = true
    }

    private fun handleSavingEntities(save: Boolean, hmGameIdToWrapper: Map<Long, GameWrapper>) {
        val wrappers = hmGameIdToWrapper.values
        if (save) {
            val games = mutableListOf<GameEntity>()
            val participants = mutableListOf<ParticipantEntity>()
            val darts = mutableListOf<DartEntity>()

            wrappers.forEach { wrapper ->
                games.add(wrapper.gameEntity!!)
                participants.add(wrapper.participantEntity!!)

                darts += wrapper.simulationDartEntities

                wrapper.clearEntities()
            }

            BulkInserter.insert(games)
            BulkInserter.insert(participants)
            BulkInserter.insert(darts)
        }
    }

    private fun getParentWindowForResults(title: String, modal: Boolean): Window {
        if (modal) {
            val dlg = JDialog()
            dlg.isModal = true
            dlg.title = title
            return dlg
        }

        return JFrame(title)
    }
}
