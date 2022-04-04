package dartzee.player

import dartzee.db.PlayerEntity
import dartzee.screen.HumanConfigurationDialog
import dartzee.screen.ScreenCache
import dartzee.screen.ai.AIConfigurationDialog
import dartzee.screen.ai.AISimulationSetupDialog

class PlayerManager
{
    fun createNewPlayer(human: Boolean) = if (human) createNewHuman() else createNewAI()
    private fun createNewHuman()
    {
        val dlg = HumanConfigurationDialog()
        dlg.setLocationRelativeTo(ScreenCache.mainScreen)
        dlg.isVisible = true
    }
    private fun createNewAI()
    {
        val dialog = AIConfigurationDialog()
        dialog.setLocationRelativeTo(ScreenCache.mainScreen)
        dialog.isVisible = true
    }

    fun amendPlayer(player: PlayerEntity)
    {
        if (player.isAi())
        {
            AIConfigurationDialog.amendPlayer(player)
        }
        else
        {
            HumanConfigurationDialog.amendPlayer(player)
        }
    }

    fun runSimulation(player: PlayerEntity)
    {
        AISimulationSetupDialog(player, player.getModel()).isVisible = true
    }
}