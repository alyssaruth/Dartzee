package dartzee.screen

import dartzee.db.PlayerEntity
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel

class HumanCreationDialog : AbstractPlayerCreationDialog()
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
    }

    fun init()
    {
        createdPlayer = false
        textFieldName.text = ""
        avatar.init(null, false)
    }

    override fun savePlayer()
    {
        val name = textFieldName.text
        val avatarId = avatar.avatarId

        PlayerEntity.factoryAndSaveHuman(name, avatarId)

        createdPlayer = true

        //Now dispose the window
        dispose()
    }
}
