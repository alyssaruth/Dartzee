package dartzee.screen.dartzee

import dartzee.core.bean.addGhostText
import dartzee.core.screen.SimpleDialog
import dartzee.core.util.DialogUtil
import dartzee.core.util.setFontSize
import dartzee.db.DARTZEE_TEMPLATE
import dartzee.db.DartzeeRuleEntity
import dartzee.db.DartzeeTemplateEntity
import dartzee.screen.ScreenCache
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

class DartzeeTemplateDialog : SimpleDialog()
{
    var dartzeeTemplate: DartzeeTemplateEntity? = null

    private val namePanel = JPanel()
    val tfName = JTextField()
    val rulePanel = DartzeeRuleSetupPanel()

    init
    {
        title = "New Dartzee Template"
        size = Dimension(800, 600)
        isModal = true

        add(namePanel, BorderLayout.NORTH)
        add(rulePanel, BorderLayout.CENTER)

        tfName.setFontSize(20)
        tfName.addGhostText("Template Name")

        namePanel.layout = BorderLayout(0, 0)
        namePanel.border = EmptyBorder(10, 5, 5, 67)
        namePanel.add(tfName)
    }

    override fun okPressed()
    {
        if (!valid())
        {
            return
        }

        val template = DartzeeTemplateEntity.factoryAndSave(tfName.text)

        val rules = rulePanel.getRules()
        rules.forEachIndexed { ix, rule ->
            val entity = rule.toEntity(ix + 1, DARTZEE_TEMPLATE, template.rowId)
            entity.saveToDatabase()
        }

        dartzeeTemplate = template

        dispose()
    }

    private fun valid(): Boolean
    {
        if (tfName.text.isEmpty())
        {
            DialogUtil.showError("You must enter a name.")
            tfName.requestFocus()
            return false
        }

        if (rulePanel.getRules().size < 2)
        {
            DialogUtil.showError("You must create at least 2 rules.")
            return false
        }

        return true
    }


    fun copy(templateToCopy: DartzeeTemplateEntity)
    {
        tfName.text = "${templateToCopy.name} - Copy"

        val rules = DartzeeRuleEntity().retrieveForTemplate(templateToCopy.rowId)

        val dtos = rules.map { it.toDto() }
        rulePanel.addRulesToTable(dtos)
    }

    companion object
    {
        fun createTemplate(templateToCopy: DartzeeTemplateEntity? = null): DartzeeTemplateEntity?
        {
            val dlg = DartzeeTemplateDialog()
            templateToCopy?.let { dlg.copy(it) }
            dlg.setLocationRelativeTo(ScreenCache.mainScreen)
            dlg.isVisible = true
            return dlg.dartzeeTemplate
        }
    }
}