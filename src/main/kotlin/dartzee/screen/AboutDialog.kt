package dartzee.screen

import dartzee.core.bean.LinkLabel
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings
import java.awt.Font
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.SwingConstants

class AboutDialog : JDialog(), ActionListener
{
    private val lblProductDesc = JLabel("Darts $DARTS_VERSION_NUMBER")
    private val lblViewChangelog = LinkLabel("Change Log", ::linkClicked)
    private val btnOk = JButton("Ok")

    init
    {
        title = "About"
        setSize(230, 175)
        isModal = InjectedThings.allowModalDialogs
        isResizable = false
        contentPane.layout = null
        lblProductDesc.font = Font("Tahoma", Font.PLAIN, 12)
        lblProductDesc.setBounds(15, 8, 184, 25)
        lblProductDesc.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblProductDesc)
        val lblCreatedBy = JLabel("Created by Alyssa Burlton")
        lblCreatedBy.font = Font("Tahoma", Font.PLAIN, 12)
        lblCreatedBy.setBounds(0, 29, 214, 25)
        lblCreatedBy.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblCreatedBy)
        lblViewChangelog.setBounds(65, 65, 84, 25)
        lblViewChangelog.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblViewChangelog)
        btnOk.margin = Insets(0, 0, 0, 0)
        btnOk.setBounds(85, 107, 45, 25)
        contentPane.add(btnOk)

        btnOk.addActionListener(this)
    }

    private fun linkClicked()
    {
        isVisible = false
        ChangeLog().also { it.isVisible = true }
    }

    override fun actionPerformed(arg0: ActionEvent) = dispose()
}