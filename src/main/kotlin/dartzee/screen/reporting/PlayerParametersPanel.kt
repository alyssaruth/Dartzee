package dartzee.screen.reporting

import dartzee.core.bean.ComboBoxNumberComparison
import dartzee.core.util.DialogUtil
import dartzee.core.util.StringUtil
import dartzee.db.MAX_PLAYERS
import dartzee.db.PlayerEntity
import dartzee.reporting.COMPARATOR_SCORE_UNSET
import dartzee.reporting.IncludedPlayerParameters
import net.miginfocom.swing.MigLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class PlayerParametersPanel : JPanel(), ActionListener
{
    val chckbxFinalScore = JCheckBox("Game Score")
    val comboBox = ComboBoxNumberComparison()
    val spinner = JSpinner(SpinnerNumberModel(3, 3, 200, 1))
    val chckbxPosition = JCheckBox("Position")
    val positionCheckboxes = List(MAX_PLAYERS) { ix -> JCheckBox(StringUtil.convertOrdinalToText(ix + 1)) }
    val cbUndecided = JCheckBox("Undecided")

    init
    {
        layout = MigLayout("", "[][][]", "[][]")

        comboBox.addOption(COMPARATOR_SCORE_UNSET)

        add(chckbxFinalScore, "cell 0 0")
        add(comboBox, "width 80:80:80, cell 1 0,growx")
        add(chckbxPosition, "cell 0 1")
        positionCheckboxes.forEach { add(it, "cell 1 1") }
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
            DialogUtil.showError("You must select at least one finishing position for player " + player.name)
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

        positionCheckboxes.forEachIndexed { ix, cb ->
            addValueIfSelected(ret, cb, ix + 1)
        }

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
        updatePlayerOptionsEnabled()
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
        positionCheckboxes.forEach { it.isEnabled = chckbxPosition.isSelected }
        cbUndecided.isEnabled = chckbxPosition.isSelected

        comboBox.isEnabled = chckbxFinalScore.isSelected

        val comboSelection = comboBox.selectedItem as String
        val unset = comboSelection == COMPARATOR_SCORE_UNSET
        spinner.isEnabled = chckbxFinalScore.isSelected && !unset
    }
}
