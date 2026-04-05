package dartzee.screen

import dartzee.db.PlayerEntity
import dartzee.utils.InjectedThings
import java.awt.BorderLayout

class HumanConfigurationDialog(
    saveCallback: (player: PlayerEntity) -> Unit,
    player: PlayerEntity = PlayerEntity.factoryCreate(),
) : AbstractPlayerConfigurationDialog(saveCallback, player) {
    init {
        setSize(450, 300)
        isResizable = false
        isModal = InjectedThings.allowModalDialogs

        contentPane.add(demographicsPanel, BorderLayout.CENTER)

        title = if (player.retrievedFromDb) "Amend Player" else "New Player"
    }

    companion object {
        fun amendPlayer(saveCallback: (PlayerEntity) -> Unit, player: PlayerEntity) {
            val dlg = HumanConfigurationDialog(saveCallback, player)
            dlg.setLocationRelativeTo(ScreenCache.mainScreen)
            dlg.isVisible = true
        }
    }
}
