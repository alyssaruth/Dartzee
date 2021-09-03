package dartzee.screen.dartzee

import dartzee.bean.DartzeeDartRuleSelector
import dartzee.bean.DartzeeAggregateRuleSelector
import dartzee.core.bean.RadioButtonPanel
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.core.util.setFontSize
import dartzee.dartzee.DartzeeRandomiser
import dartzee.dartzee.DartzeeRuleDto
import dartzee.db.MAX_RULE_NAME
import dartzee.screen.ScreenCache
import net.miginfocom.swing.MigLayout
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class DartzeeRuleCreationDialog(private val verificationPanel: DartzeeRuleVerificationPanel = DartzeeRuleVerificationPanel()): SimpleDialog(), ChangeListener
{
    var dartzeeRule: DartzeeRuleDto? = null

    val lblDifficulty = JLabel()
    private val panelCenter = JPanel()
    private val panelRuleStrength = JPanel()
    private val panelDarts = JPanel()
    private val rdbtnPanelDartScoreType = RadioButtonPanel()
    val rdbtnAllDarts = JRadioButton("All Darts")
    val dartOneSelector = DartzeeDartRuleSelector("Dart 1")
    val dartTwoSelector = DartzeeDartRuleSelector("Dart 2")
    val dartThreeSelector = DartzeeDartRuleSelector("Dart 3")
    val cbInOrder = JCheckBox("In Order")
    val targetSelector = DartzeeDartRuleSelector("Target")
    val rdbtnAtLeastOne = JRadioButton("At least one dart")
    val rdbtnNoDarts = JRadioButton("No darts")
    private val panelTotal = JPanel()
    private val panelAllowMisses = JPanel()
    val cbAllowMisses = JCheckBox("Allow misses")
    val aggregateSelector = DartzeeAggregateRuleSelector("Other")
    private val panelRuleDescription = JPanel()
    val tfDescription = JTextField()
    val btnRandom = JButton()
    private val panelRuleName = JPanel()
    private val cbRuleName = JCheckBox("Custom rule name")
    private val tfRuleName = JTextField()

    init
    {
        title = "Add Dartzee Rule"
        setSize(900, 640)
        setLocationRelativeTo(ScreenCache.mainScreen)
        isModal = true

        add(panelRuleDescription, BorderLayout.NORTH)
        add(panelCenter, BorderLayout.CENTER)
        add(verificationPanel, BorderLayout.EAST)

        lblDifficulty.isOpaque = true
        lblDifficulty.setFontSize(24)
        panelRuleStrength.add(lblDifficulty)

        panelCenter.layout = MigLayout("", "[grow]", "[grow][grow][grow]")
        panelCenter.add(panelRuleStrength, "cell 0 1, growx")
        panelCenter.add(panelRuleName, "cell 0 2, growx")
        panelCenter.add(panelDarts, "cell 0 3, growx")
        panelCenter.add(panelTotal, "cell 0 4, growx")
        panelCenter.add(panelAllowMisses, "cell 0 5, growx")

        tfRuleName.columns = 30
        panelRuleName.layout = MigLayout("", "[]", "[]")
        panelRuleName.add(cbRuleName, "cell 0 0")
        panelRuleName.add(tfRuleName, "cell 1 0, growx")

        panelDarts.layout = MigLayout("", "[][]", "[][][][]")
        rdbtnPanelDartScoreType.add(rdbtnAllDarts)
        rdbtnPanelDartScoreType.add(rdbtnAtLeastOne)
        rdbtnPanelDartScoreType.add(rdbtnNoDarts)
        panelDarts.add(rdbtnPanelDartScoreType, "spanx")
        panelDarts.validate()

        panelTotal.layout = MigLayout("", "[]", "[]")

        panelTotal.add(aggregateSelector, "cell 0 0")

        panelAllowMisses.layout = MigLayout("", "[]", "[]")
        panelAllowMisses.add(cbAllowMisses, "cell 0 0")

        panelRuleDescription.layout = BorderLayout(0,0)
        panelRuleDescription.border = TitledBorder("")
        panelRuleDescription.add(tfDescription, BorderLayout.CENTER)
        panelRuleDescription.add(btnRandom, BorderLayout.EAST)
        btnRandom.preferredSize = Dimension(50, 50)
        btnRandom.icon = ImageIcon(javaClass.getResource("/buttons/dice.png"))
        btnRandom.toolTipText = "Generate random rule"
        tfDescription.preferredSize = Dimension(900, 50)

        tfDescription.horizontalAlignment = JTextField.CENTER
        tfDescription.setFontSize(24)
        tfDescription.isEditable = false

        cbRuleName.addActionListener(this)
        rdbtnPanelDartScoreType.addActionListener(this)
        dartOneSelector.addActionListener(this)
        dartTwoSelector.addActionListener(this)
        dartThreeSelector.addActionListener(this)
        targetSelector.addActionListener(this)
        aggregateSelector.addActionListener(this)
        cbInOrder.addActionListener(this)
        cbAllowMisses.addActionListener(this)
        btnRandom.addActionListener(this)

        cbInOrder.isSelected = true

        updateComponents()
    }

    fun amendRule(rule: DartzeeRuleDto)
    {
        title = "Amend Dartzee Rule"
        this.dartzeeRule = rule

        populate(rule)
    }

    fun populate(rule: DartzeeRuleDto)
    {
        if (rule.dart1Rule == null)
        {
            rdbtnNoDarts.isSelected = true
        }
        else if (rule.dart2Rule == null)
        {
            rdbtnAtLeastOne.isSelected = true

            targetSelector.populate(rule.dart1Rule)
        }
        else
        {
            rdbtnAllDarts.isSelected = true
            cbInOrder.isSelected = rule.inOrder

            dartOneSelector.populate(rule.dart1Rule)
            dartTwoSelector.populate(rule.dart2Rule)
            dartThreeSelector.populate(rule.dart3Rule!!)
        }

        aggregateSelector.populate(rule.aggregateRule)

        cbAllowMisses.isSelected = rule.allowMisses

        updateComponents()
        repaint()
    }

    override fun stateChanged(e: ChangeEvent?)
    {
        updateComponents()
    }

    override fun actionPerformed(arg0: ActionEvent)
    {
        if (arg0.source == btnRandom)
        {
            populate(DartzeeRandomiser.generateRandomRule())
        }
        else if (arg0.source !in listOf(btnOk, btnCancel))
        {
            updateComponents()
        }
        else
        {
            super.actionPerformed(arg0)
        }
    }

    override fun okPressed()
    {
        if (!valid())
        {
            return
        }

        val rule = constructRuleFromComponents()

        val calculationResult = rule.runStrengthCalculation()
        val combinations = calculationResult.validCombinations
        if (combinations == 0)
        {
            DialogUtil.showError("This rule is impossible!")
            return
        }

        dartzeeRule = rule

        dispose()
    }

    fun constructRuleFromComponents(): DartzeeRuleDto
    {
        val totalRule = if (aggregateSelector.isEnabled) aggregateSelector.getSelection() else null
        val ruleName = if (cbRuleName.isSelected) tfRuleName.text else null

        return if (rdbtnAllDarts.isSelected)
        {
            DartzeeRuleDto(dartOneSelector.getSelection(),
                    dartTwoSelector.getSelection(), dartThreeSelector.getSelection(), totalRule, cbInOrder.isSelected, cbAllowMisses.isSelected, ruleName)
        }
        else
        {
            val dart1Rule = if (rdbtnAtLeastOne.isSelected) targetSelector.getSelection() else null
            DartzeeRuleDto(dart1Rule, null, null, totalRule, false, cbAllowMisses.isSelected, ruleName)
        }
    }

    private fun valid(): Boolean
    {
        if (cbRuleName.isSelected)
        {
            val ruleName = tfRuleName.text

            if (ruleName.isBlank())
            {
                DialogUtil.showError("You cannot have an empty rule name.")
                return false
            }

            if (ruleName.length > MAX_RULE_NAME)
            {
                DialogUtil.showError("Rule name cannot exceed $MAX_RULE_NAME characters.")
                return false
            }
        }

        return if (rdbtnAtLeastOne.isSelected)
        {
            targetSelector.valid()
        }
        else
        {
            dartOneSelector.valid() && dartTwoSelector.valid() && dartThreeSelector.valid()
        }
    }

    private fun updateComponents()
    {
        tfRuleName.isEnabled = cbRuleName.isSelected

        if (rdbtnAllDarts.isSelected)
        {
            panelDarts.remove(targetSelector)
            panelDarts.add(dartOneSelector, "cell 0 1")
            panelDarts.add(dartTwoSelector, "cell 0 2")
            panelDarts.add(dartThreeSelector, "cell 0 3")
            panelDarts.add(cbInOrder, "cell 0 4")
        }
        else
        {
            panelDarts.remove(dartOneSelector)
            panelDarts.remove(dartTwoSelector)
            panelDarts.remove(dartThreeSelector)
            panelDarts.remove(cbInOrder)

            if (rdbtnAtLeastOne.isSelected)
            {
                panelDarts.add(targetSelector, "cell 0 1")
            }
            else
            {
                panelDarts.remove(targetSelector)
            }
        }

        repaint()
        panelDarts.revalidate()

        SwingUtilities.invokeLater {
            val rule = constructRuleFromComponents()
            val ruleName = rule.generateRuleDescription()
            tfDescription.text = ruleName

            val calculationResult = rule.runStrengthCalculation()
            lblDifficulty.text = calculationResult.getDifficultyDesc()
            lblDifficulty.foreground = calculationResult.getForeground()
            lblDifficulty.background = calculationResult.getBackground()
            panelRuleStrength.background = calculationResult.getBackground()

            verificationPanel.updateRule(rule)
        }
    }
}