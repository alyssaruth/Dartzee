package dartzee.screen

import dartzee.achievements.AbstractAchievement
import dartzee.achievements.getAllAchievements
import dartzee.achievements.runConversionsWithProgressBar
import dartzee.bean.PlayerSelector
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.utils.InjectedThings
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.util.*
import javax.swing.DefaultComboBoxModel
import javax.swing.JComboBox
import javax.swing.JOptionPane
import javax.swing.JRadioButton

class AchievementConversionDialog : SimpleDialog()
{
    private val panelTop = RadioButtonPanel()
    private val rdbtnAll = JRadioButton("All")
    private val rdbtnSpecific = JRadioButton("Specific conversion")
    private val cbConversionType = JComboBox<AbstractAchievement>()
    private val playerSelector = PlayerSelector()

    init
    {
        title = "Achievement Conversion"
        setSize(600, 400)
        isResizable = false
        isModal = InjectedThings.allowModalDialogs

        contentPane.add(playerSelector, BorderLayout.CENTER)
        contentPane.add(panelTop, BorderLayout.NORTH)

        playerSelector.init()

        panelTop.layout = MigLayout("", "[69px][101px][grow][45px][][][]", "[25px][grow]")
        panelTop.add(rdbtnAll, "flowy,cell 0 0,alignx left,aligny top")
        panelTop.add(rdbtnSpecific, "cell 0 1,alignx left,aligny center")
        panelTop.add(cbConversionType, "flowx,cell 1 1")
        panelTop.addActionListener(this)

        cbConversionType.isEnabled = false
        initComboBox()
    }

    private fun initComboBox()
    {
        val achievements = Vector(getAllAchievements())
        cbConversionType.model = DefaultComboBoxModel(achievements)
    }

    override fun okPressed()
    {
        if (!valid())
        {
            return
        }

        val selectedPlayerIds = playerSelector.getSelectedPlayers().map { it.rowId }
        if (rdbtnAll.isSelected)
        {
            val achievements = getAllAchievements()
            runConversionsWithProgressBar(achievements, selectedPlayerIds)
        }
        else
        {
            val ix = cbConversionType.selectedIndex
            val achievement = cbConversionType.getItemAt(ix)

            runConversionsWithProgressBar(listOf(achievement), selectedPlayerIds)
        }

        dispose()
    }

    private fun valid() : Boolean
    {
        if (playerSelector.getSelectedPlayers().isEmpty())
        {
            val ans = DialogUtil.showQuestionOLD("This will run the conversion(s) for ALL players. Proceed?", false)
            return ans == JOptionPane.YES_OPTION
        }

        return true
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (panelTop.isEventSource(arg0))
        {
            cbConversionType.isEnabled = rdbtnSpecific.isSelected
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }
}
