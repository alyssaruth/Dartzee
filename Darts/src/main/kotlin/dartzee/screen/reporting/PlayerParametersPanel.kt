package dartzee.screen.reporting

import dartzee.core.util.Debug
import dartzee.db.PlayerEntity
import dartzee.reporting.COMPARATOR_SCORE_UNSET
import dartzee.reporting.IncludedPlayerParameters
import dartzee.core.bean.ComboBoxNumberComparison
import dartzee.core.util.DialogUtil
import net.miginfocom.swing.MigLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class PlayerParametersPanel : JPanel(), ActionListener
{
    private val chckbxFinalScore = JCheckBox("Game Score")
    private val comboBox = ComboBoxNumberComparison()
    private val spinner = JSpinner(SpinnerNumberModel(3, 3, 200, 1))
    private val chckbxPosition = JCheckBox("Position")
    private val cbFirst = JCheckBox("1st")
    private val cbSecond = JCheckBox("2nd")
    private val cbThird = JCheckBox("3rd")
    private val cbFourth = JCheckBox("4th")
    private val cbUndecided = JCheckBox("Undecided")

    init
    {

        layout = MigLayout("", "[][][]", "[][]")

        comboBox.addOption(COMPARATOR_SCORE_UNSET)

        add(chckbxFinalScore, "cell 0 0")
        add(comboBox, "width 80:80:80, cell 1 0,growx")
        add(chckbxPosition, "cell 0 1")
        add(cbFirst, "flowx,cell 1 1")
        add(cbSecond, "cell 1 1")
        add(cbThird, "cell 1 1")
        add(cbFourth, "cell 1 1")
        add(cbUndecided, "cell 1 1")
        add(spinner, "cell 1 0")

        updatePlayerOptionsEnabled()

        chckbxFinalScore.addActionListener(this)
        chckbxPosition.addActionListener(this)
        comboBox.addActionListener(this)
    }

    fun valid(player: PlayerEntity): Boolean
    {
        if (chckbxPosition.isSelected && getFinishingPositions().isEmpty())
        {
            DialogUtil.showError("You must select at least one finishing position for Player " + player.name)
            return false
        }

        return true
    }

    fun generateParameters(): IncludedPlayerParameters
    {
        val parms = IncludedPlayerParameters()

        if (chckbxFinalScore.isSelected)
        {
            val finalScore = spinner.value as Int
            val comparator = comboBox.selectedItem as String

            parms.finalScore = finalScore
            parms.finalScoreComparator = comparator
        }

        if (chckbxPosition.isSelected)
        {
            val finishingPositions = getFinishingPositions()
            parms.finishingPositions = finishingPositions
        }

        return parms
    }

    private fun getFinishingPositions(): List<Int>
    {
        val ret = mutableListOf<Int>()

        addValueIfSelected(ret, cbFirst, 1)
        addValueIfSelected(ret, cbSecond, 2)
        addValueIfSelected(ret, cbThird, 3)
        addValueIfSelected(ret, cbFourth, 4)
        addValueIfSelected(ret, cbUndecided, -1)

        return ret
    }

    private fun addValueIfSelected(v: MutableList<Int>, cb: JCheckBox, value: Int)
    {
        if (cb.isSelected)
        {
            v.add(value)
        }
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        return when (arg0.source)
        {
            chckbxFinalScore, chckbxPosition, comboBox -> updatePlayerOptionsEnabled()
            else -> Debug.stackTrace("Unexpected actionPerformed: [${arg0.source}]")
        }
    }

    fun disableAll()
    {
        chckbxFinalScore.isSelected = false
        chckbxFinalScore.isEnabled = false
        chckbxPosition.isSelected = false
        chckbxPosition.isEnabled = false

        updatePlayerOptionsEnabled()
    }

    private fun updatePlayerOptionsEnabled()
    {
        cbFirst.isEnabled = chckbxPosition.isSelected
        cbSecond.isEnabled = chckbxPosition.isSelected
        cbThird.isEnabled = chckbxPosition.isSelected
        cbFourth.isEnabled = chckbxPosition.isSelected
        cbUndecided.isEnabled = chckbxPosition.isSelected && !chckbxFinalScore.isSelected

        comboBox.isEnabled = chckbxFinalScore.isSelected

        val comboSelection = comboBox.selectedItem as String
        val unset = comboSelection == COMPARATOR_SCORE_UNSET
        spinner.isEnabled = chckbxFinalScore.isSelected && !unset

        if (chckbxFinalScore.isSelected)
        {
            cbUndecided.isSelected = false
        }
    }
}
