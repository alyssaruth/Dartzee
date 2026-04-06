package dartzee.screen

import dartzee.core.screen.SimpleDialog
import dartzee.db.PlayerEntity
import dartzee.utils.InjectedThings
import java.awt.BorderLayout

class HumanConfigurationDialog(
    private val saveCallback: (player: PlayerEntity) -> Unit,
    private val player: PlayerEntity = PlayerEntity.factoryCreate(),
) : SimpleDialog() {
    private val demographicsPanel = PlayerDemographicsPanel(player)

    init {
        setSize(450, 300)
        isResizable = false
        isModal = InjectedThings.allowModalDialogs

        contentPane.add(demographicsPanel, BorderLayout.CENTER)

        title = if (player.retrievedFromDb) "Amend Player" else "New Player"
    }

    override fun okPressed() {
        if (demographicsPanel.valid()) {
            demographicsPanel.writeDetails()
            player.saveToDatabase()

            dispose()
            saveCallback(player)
        }
    }

    companion object {
        fun amendPlayer(saveCallback: (PlayerEntity) -> Unit, player: PlayerEntity) {
            val dlg = HumanConfigurationDialog(saveCallback, player)
            dlg.setLocationRelativeTo(ScreenCache.mainScreen)
            dlg.isVisible = true
        }
    }
}
