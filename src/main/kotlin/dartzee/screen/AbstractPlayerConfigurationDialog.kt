package dartzee.screen

import dartzee.core.screen.SimpleDialog
import dartzee.db.PlayerEntity

abstract class AbstractPlayerConfigurationDialog(
    protected val saveCallback: (player: PlayerEntity) -> Unit,
    protected val player: PlayerEntity,
) : SimpleDialog() {
    // Components
    protected val demographicsPanel = PlayerDemographicsPanel(player)

    open fun writeExtraDetails() {
        // Do nothing
    }

    override fun okPressed() {
        if (demographicsPanel.valid()) {
            demographicsPanel.writeDetails()
            writeExtraDetails()
            player.saveToDatabase()

            dispose()
            saveCallback(player)
        }
    }
}
