package dartzee.screen

import dartzee.db.PlayerEntity
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel

class HumanConfigurationDialog(player: PlayerEntity = PlayerEntity.factoryCreate()): AbstractPlayerConfigurationDialog(player)
{
    private val panel = JPanel()

    init
    {
        title = "New Player"
        setSize(350, 225)
        isResizable = false
        isModal = true
        val flowLayout = panel.layout as FlowLayout
        flowLayout.hgap = 20

        contentPane.add(panel, BorderLayout.CENTER)

        panel.add(avatar)
        panel.add(textFieldName)
        textFieldName.columns = 10

        initFields()
    }

    private fun initFields()
    {
        textFieldName.text = player.name
        avatar.init(player, false)
        avatar.readOnly = player.retrievedFromDb
        title = if (player.retrievedFromDb) "Amend Player" else "New Player"
    }

    override fun savePlayer()
    {
        val name = textFieldName.text
        val avatarId = avatar.avatarId

        player.name = name
        player.playerImageId = avatarId
        player.saveToDatabase()

        //Now dispose the window
        dispose()
    }

    companion object
    {
        fun amendPlayer(player: PlayerEntity)
        {
            val dlg = HumanConfigurationDialog(player)
            dlg.setLocationRelativeTo(ScreenCache.mainScreen)
            dlg.isVisible = true
        }
    }
}
