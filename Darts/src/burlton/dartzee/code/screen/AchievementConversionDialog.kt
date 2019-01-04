package burlton.dartzee.code.screen

import burlton.dartzee.code.achievements.AbstractAchievement
import burlton.dartzee.code.achievements.getAllAchievements
import burlton.dartzee.code.achievements.runConversionsWithProgressBar
import burlton.dartzee.code.bean.PlayerSelector
import burlton.desktopcore.code.bean.RadioButtonPanel
import burlton.desktopcore.code.screen.SimpleDialog
import burlton.desktopcore.code.util.DialogUtil
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
        isModal = true

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


        if (rdbtnAll.isSelected)
        {
            val achievements = getAllAchievements()
            runConversionsWithProgressBar(achievements, playerSelector.selectedPlayers)
        }
        else
        {
            val ix = cbConversionType.selectedIndex
            val achievement = cbConversionType.getItemAt(ix)

            runConversionsWithProgressBar(mutableListOf(achievement), playerSelector.selectedPlayers)
        }

        dispose()
    }

    private fun valid() : Boolean
    {
        if (playerSelector.selectedPlayers.isEmpty())
        {
            val ans = DialogUtil.showQuestion("This will run the conversion(s) for ALL players. Proceed?", false)
            return ans == JOptionPane.YES_OPTION
        }

        return true
    }

    override fun actionPerformed(arg0: ActionEvent?)
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
