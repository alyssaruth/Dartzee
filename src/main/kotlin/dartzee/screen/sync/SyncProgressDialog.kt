package dartzee.screen.sync

import dartzee.screen.ScreenCache
import dartzee.sync.SyncStage
import dartzee.sync.desc
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.EmptyBorder

class SyncProgressDialog() : JDialog()
{
    private val progressBar = JProgressBar()

    init
    {
        setSize(300, 90)
        isResizable = false
        setLocationRelativeTo(ScreenCache.mainScreen)
        isModal = true

        title = "Sync Progress"

        val panel = JPanel()
        panel.border = EmptyBorder(10, 0, 0, 0)
        contentPane.add(panel, BorderLayout.CENTER)
        progressBar.minimum = 0
        progressBar.maximum = SyncStage.values().size
        progressBar.preferredSize = Dimension(200, 20)
        progressBar.isStringPainted = true
        panel.add(progressBar)

        val panelCancel = JPanel()
        panelCancel.border = EmptyBorder(5, 0, 5, 0)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
    }

    fun setVisibleLater()
    {
        SwingUtilities.invokeLater { isVisible = true }
    }

    fun progressToStage(stage: SyncStage)
    {
        val stageIndex = SyncStage.values().toList().indexOf(stage)
        SwingUtilities.invokeLater {
            progressBar.value = stageIndex
            progressBar.string = "Stage ${stageIndex + 1}: ${stage.desc()}"
            progressBar.repaint()
        }
    }

    fun disposeLater()
    {
        SwingUtilities.invokeLater { dispose() }
    }

    companion object
    {
        private val instance = SyncProgressDialog()

        fun syncStarted()
        {
            instance.progressToStage(SyncStage.PULL_REMOTE)
            instance.setVisibleLater()
        }

        fun progressToStage(stage: SyncStage)
        {
            instance.progressToStage(stage)
        }

        fun dispose()
        {
            instance.disposeLater()
        }

        fun isVisible() = instance.isVisible
    }
}
