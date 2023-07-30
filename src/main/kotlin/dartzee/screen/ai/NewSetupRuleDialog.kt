package dartzee.screen.ai

import dartzee.ai.AimDart
import dartzee.bean.SpinnerSingleSelector
import dartzee.core.bean.NumberField
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.`object`.Dart
import dartzee.screen.ScreenCache
import dartzee.utils.isBust
import getDefaultDartToAimAt
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.JLabel
import javax.swing.JRadioButton

class NewSetupRuleDialog(private val hmScoreToDart: MutableMap<Int, AimDart>) : SimpleDialog()
{
    val panel = RadioButtonPanel()
    private val lblScore = JLabel("Score")
    val nfScore = NumberField(4, 501)
    private val lblAimFor = JLabel("Aim for")
    val rdbtnSingle = JRadioButton("Single")
    val rdbtnDouble = JRadioButton("Double")
    val rdbtnTreble = JRadioButton("Treble")
    val spinner = SpinnerSingleSelector()

    init
    {
        title = "Add Rule"
        setSize(200, 200)
        isResizable = false
        isModal = true

        contentPane.add(panel, BorderLayout.CENTER)
        panel.layout = MigLayout("", "[][][]", "[][][][]")
        panel.add(lblScore, "cell 0 0,alignx trailing")
        panel.add(nfScore, "cell 1 0,growx")
        panel.add(lblAimFor, "cell 0 1")
        rdbtnSingle.preferredSize = Dimension(53, 25)
        panel.add(rdbtnSingle, "cell 1 1")
        panel.add(spinner, "cell 2 1")
        rdbtnDouble.preferredSize = Dimension(59, 25)
        panel.add(rdbtnDouble, "cell 1 2")
        rdbtnTreble.preferredSize = Dimension(55, 25)
        panel.add(rdbtnTreble, "cell 1 3")

        rdbtnSingle.addActionListener(this)
        rdbtnDouble.addActionListener(this)
        rdbtnTreble.addActionListener(this)

        pack()
    }

    private fun getDartFromSelections(): AimDart
    {
        val multiplier = getMultiplier()
        val value = spinner.value as Int
        return AimDart(value, multiplier)
    }
    private fun getMultiplier(): Int
    {
        return when (panel.selection)
        {
            rdbtnSingle -> 1
            rdbtnDouble -> 2
            else -> 3
        }
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (panel.isEventSource(arg0))
        {
            panel.remove(spinner)

            val multiplier = getMultiplier()
            panel.add(spinner, "cell 2 $multiplier")

            pack()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    override fun okPressed()
    {
        if (valid())
        {
            val score = nfScore.getNumber()
            val drt = getDartFromSelections()

            hmScoreToDart[score] = drt

            dispose()
        }
    }

    fun valid(): Boolean
    {
        val score = nfScore.getNumber()
        if (score == -1)
        {
            DialogUtil.showErrorOLD("You must enter a score for this rule to apply to.")
            return false
        }

        val drt = getDartFromSelections()
        if (drt.score == 25 && drt.multiplier == 3)
        {
            DialogUtil.showErrorOLD("Treble 25 is not a valid dart!")
            return false
        }

        if (isBust(score, Dart(drt.score, drt.multiplier)))
        {
            DialogUtil.showErrorOLD("This target would bust the player")
            return false
        }

        //If we're specifying a rule for under 60, validate whether what we're setting up is
        //already the default
        if (score <= 60)
        {
            val defaultDart = getDefaultDartToAimAt(score)
            if (defaultDart == drt)
            {
                DialogUtil.showErrorOLD("The selected dart is already the default for this starting score.")
                return false
            }
        }

        return true
    }

    companion object
    {
        fun addNewSetupRule(hmScoreToDart: MutableMap<Int, AimDart>)
        {
            val dlg = NewSetupRuleDialog(hmScoreToDart)
            dlg.setLocationRelativeTo(ScreenCache.mainScreen)
            dlg.isVisible = true
        }
    }
}
