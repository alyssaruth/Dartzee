package dartzee.core.screen

import dartzee.core.util.Debug
import dartzee.core.obj.LimitedDocument
import dartzee.core.util.DialogUtil
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.border.LineBorder

class BugReportDialog : SimpleDialog()
{
    val descriptionField = JTextField()
    val textPaneReplicationSteps = JTextPane()

    init
    {
        title = "Bug Report"
        setSize(400, 400)
        minimumSize = Dimension(400, 400)
        val panel_1 = JPanel()
        val flowLayout = panel_1.layout as FlowLayout
        flowLayout.alignment = FlowLayout.LEFT
        contentPane.add(panel_1, BorderLayout.NORTH)

        val lblDescriptionconcise = JLabel("Description*")
        panel_1.add(lblDescriptionconcise)

        val horizontalStrut = Box.createHorizontalStrut(5)
        panel_1.add(horizontalStrut)
        descriptionField.preferredSize = Dimension(280, 30)
        panel_1.add(descriptionField)
        descriptionField.document = LimitedDocument(40)
        val panelCenter = JPanel()
        contentPane.add(panelCenter, BorderLayout.CENTER)
        panelCenter.layout = BorderLayout(0, 0)

        val panelNorth = JPanel()
        val flowLayoutNorth = panelNorth.layout as FlowLayout
        flowLayoutNorth.alignment = FlowLayout.LEFT
        panelCenter.add(panelNorth, BorderLayout.NORTH)

        val lblReplicationSteps = JLabel("Additional Information / Replication Steps:")
        lblReplicationSteps.horizontalAlignment = SwingConstants.LEFT
        panelNorth.add(lblReplicationSteps)

        val panel_4 = JPanel()
        panel_4.border = EmptyBorder(5, 5, 5, 5)
        panelCenter.add(panel_4, BorderLayout.CENTER)
        panel_4.layout = BorderLayout(0, 0)

        textPaneReplicationSteps.border = LineBorder(Color.GRAY)

        val scrollpane = JScrollPane()
        scrollpane.setViewportView(textPaneReplicationSteps)
        panel_4.add(scrollpane, BorderLayout.CENTER)
    }

    private fun valid(): Boolean
    {
        val description = descriptionField.text
        if (description == null || description.isEmpty())
        {
            DialogUtil.showError("You must enter a description.")
            return false
        }

        return true
    }

    private fun sendReport()
    {
        val description = "BUG REPORT: " + descriptionField.text
        val replication = textPaneReplicationSteps.text

        if (Debug.sendBugReport(description, replication))
        {
            DialogUtil.showInfo("Bug report submitted.")
            dispose()
        }
        else
        {
            DialogUtil.showInfo("Unable to send bug report. Please check your internet connection and try again.")
        }
    }

    override fun okPressed()
    {
        if (valid())
        {
            sendReport()
        }
    }
}
