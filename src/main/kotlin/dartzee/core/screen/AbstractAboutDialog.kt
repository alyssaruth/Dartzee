package dartzee.core.screen

import dartzee.core.bean.HyperlinkAdaptor
import dartzee.core.bean.IHyperlinkListener
import java.awt.Color
import java.awt.Font
import java.awt.Insets
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.SwingConstants

abstract class AbstractAboutDialog : JDialog(), IHyperlinkListener, ActionListener
{
    private val lblProductDesc = JLabel(getProductDesc())
    private val lblViewChangelog = JLabel("<html><u>Change Log</u></html>")
    private val btnOk = JButton("Ok")

    init
    {
        title = "About"
        setSize(230, 175)
        isResizable = false
        contentPane.layout = null
        lblProductDesc.font = Font("Tahoma", Font.PLAIN, 12)
        lblProductDesc.setBounds(15, 8, 184, 25)
        lblProductDesc.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblProductDesc)
        val lblCreatedByAlex = JLabel("Created by Alex Burlton")
        lblCreatedByAlex.font = Font("Tahoma", Font.PLAIN, 12)
        lblCreatedByAlex.setBounds(0, 29, 214, 25)
        lblCreatedByAlex.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblCreatedByAlex)
        lblViewChangelog.setBounds(65, 65, 84, 25)
        lblViewChangelog.foreground = Color.BLUE
        lblViewChangelog.font = Font("Tahoma", Font.BOLD, 12)
        lblViewChangelog.horizontalAlignment = SwingConstants.CENTER
        contentPane.add(lblViewChangelog)
        btnOk.margin = Insets(0, 0, 0, 0)
        btnOk.setBounds(85, 107, 45, 25)
        contentPane.add(btnOk)

        val adaptor = HyperlinkAdaptor(this)
        lblViewChangelog.addMouseListener(adaptor)
        lblViewChangelog.addMouseMotionListener(adaptor)

        btnOk.addActionListener(this)
    }

    //Abstract functions
    abstract fun getProductDesc(): String
    abstract fun getChangeLog(): Window

    /**
     * HyperlinkListener
     */
    override fun linkClicked(arg0: MouseEvent)
    {
        isVisible = false
        val dialog = getChangeLog()
        dialog.isVisible = true
    }

    override fun isOverHyperlink(arg0: MouseEvent) = true
    override fun actionPerformed(arg0: ActionEvent) = dispose()
}