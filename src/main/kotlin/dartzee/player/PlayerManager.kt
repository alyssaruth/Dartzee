package dartzee.player

import dartzee.db.PlayerEntity
import dartzee.screen.HumanCreationDialog
import dartzee.screen.ScreenCache
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.ai.AISimulationSetup

class PlayerManager
{
    fun createNewPlayer(human: Boolean) = if (human) createNewHuman() else createNewAI()
    private fun createNewHuman(): Boolean
    {
        val dlg = HumanCreationDialog()
        dlg.isVisible = true

        return dlg.createdPlayer
    }
    private fun createNewAI(): Boolean
    {
        val dialog = AIConfigurationDialog()
        dialog.setLocationRelativeTo(ScreenCache.mainScreen)
        dialog.isVisible = true

        return dialog.createdPlayer
    }

    fun amendPlayer(player: PlayerEntity)
    {
        AIConfigurationDialog.amendPlayer(player)
    }

    fun runSimulation(player: PlayerEntity)
    {
        AISimulationSetup(player).isVisible = true
    }
}