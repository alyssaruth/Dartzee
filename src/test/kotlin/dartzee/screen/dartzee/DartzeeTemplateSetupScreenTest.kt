package dartzee.screen.dartzee

import dartzee.core.bean.ScrollTable
import dartzee.core.helper.processKeyPress
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.aggregate.DartzeeTotalRulePrime
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.findQuestionDialog
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.innerOuterInner
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertGame
import dartzee.helper.insertTemplateAndRule
import dartzee.helper.makeDartzeeRuleDto
import dartzee.helper.totalIsFifty
import io.github.alyssaruth.swingtest.clickCancel
import io.github.alyssaruth.swingtest.clickChild
import io.github.alyssaruth.swingtest.clickOk
import io.github.alyssaruth.swingtest.dismissDialog
import io.github.alyssaruth.swingtest.expectQuestionDialog
import io.github.alyssaruth.swingtest.getChild
import io.github.alyssaruth.swingtest.getWindow
import io.github.alyssaruth.swingtest.shouldBeDisabled
import io.github.alyssaruth.swingtest.shouldBeEnabled
import io.github.alyssaruth.swingtest.typeIntoInputDialog
import io.github.alyssaruth.swingtest.typeText
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JTextField
import org.junit.jupiter.api.Test

class DartzeeTemplateSetupScreenTest : AbstractTest() {
    @Test
    fun `Should pull through a game count of 0 for templates where no games have been played`() {
        insertTemplateAndRule()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getGameCount(0) shouldBe 0
    }

    @Test
    fun `Should pull through the right game count for templates where games have been played`() {
        val templateId = insertTemplateAndRule().rowId

        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)
        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getGameCount(0) shouldBe 2
    }

    @Test
    fun `Should toggle copy, remove and rename buttons based on whether a row is selected`() {
        insertTemplateAndRule()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<JButton>("copy").shouldBeDisabled()
        scrn.getChild<JButton>("deleteTemplate").shouldBeDisabled()
        scrn.getChild<JButton>("rename").shouldBeDisabled()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.getChild<JButton>("copy").shouldBeEnabled()
        scrn.getChild<JButton>("deleteTemplate").shouldBeEnabled()
        scrn.getChild<JButton>("rename").shouldBeEnabled()

        scrn.getChild<ScrollTable>().selectRow(-1)
        scrn.getChild<JButton>("copy").shouldBeDisabled()
        scrn.getChild<JButton>("deleteTemplate").shouldBeDisabled()
        scrn.getChild<JButton>("rename").shouldBeDisabled()
    }

    @Test
    fun `Should leave template alone if delete is cancelled`() {
        insertTemplateAndRule(name = "ABC")

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("deleteTemplate")

        expectQuestionDialog("Are you sure you want to delete the ABC Template?", "No")

        scrn.getChild<ScrollTable>().rowCount shouldBe 1
        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 1
    }

    @Test
    fun `Should delete a template and associated rules on confirmation`() {
        insertTemplateAndRule(name = "ABC")

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("deleteTemplate")

        expectQuestionDialog("Are you sure you want to delete the ABC Template?", "Yes")

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 0
        getCountFromTable(EntityName.DartzeeRule) shouldBe 0
    }

    @Test
    fun `Should support deleting by using the keyboard shortcut`() {
        insertTemplateAndRule(name = "ABC")

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.getChild<ScrollTable>().processKeyPress(KeyEvent.VK_DELETE)

        expectQuestionDialog("Are you sure you want to delete the ABC Template?", "Yes")

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 0
        getCountFromTable(EntityName.DartzeeRule) shouldBe 0
    }

    @Test
    fun `Pressing delete with no row selected should do nothing`() {
        insertTemplateAndRule(name = "ABC")

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(-1)
        scrn.getChild<ScrollTable>().processKeyPress(KeyEvent.VK_DELETE)

        findQuestionDialog().shouldBeNull()
        scrn.getChild<ScrollTable>().rowCount shouldBe 1
    }

    @Test
    fun `Should revert games to Custom on deletion and show a different confirmation message`() {
        val templateId = insertTemplateAndRule(name = "ABC").rowId

        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)
        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("deleteTemplate")

        expectQuestionDialog("You have played 2 games using the ABC Template." +
                "\nThese will become custom games if you delete it. Are you sure you want to continue?", "Yes")

        GameEntity().retrieveEntities().forEach { it.gameParams shouldBe "" }
    }

    @Test
    fun `Should not add a template if cancelled`() {
        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()
        scrn.clickChild<JButton>("add")

        val dlg = getWindow<DartzeeTemplateDialog>()
        dlg.clickCancel()

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
    }

    @Test
    fun `Should add a template to the table upon creation`() {
        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
        scrn.clickChild<JButton>("add")

        val dlg = getWindow<DartzeeTemplateDialog>()
        dlg.getChild<JTextField>().typeText("BTBF's House Party")
        dlg.rulePanel.addRulesToTable(listOf(innerOuterInner, totalIsFifty))
        dlg.clickOk()

        scrn.getChild<ScrollTable>().rowCount shouldBe 1
        scrn.getTemplate(0).name shouldBe "BTBF's House Party"
    }

    @Test
    fun `Should do nothing if template copying is cancelled`() {
        insertTemplateAndRule(name = "ABC")

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("copy")

        val dlg = getWindow<DartzeeTemplateDialog>()
        dlg.clickCancel()

        scrn.getChild<ScrollTable>().rowCount shouldBe 1
    }

    @Test
    fun `Should copy a template and add it to the table`() {
        insertTemplateAndRule(name = "ABC")

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("copy")

        val dlg = getWindow<DartzeeTemplateDialog>()
        dlg.clickOk()

        scrn.getChild<ScrollTable>().rowCount shouldBe 2
        val templates = listOf(scrn.getTemplate(0), scrn.getTemplate(1)).map { it.name }
        templates.shouldContainExactlyInAnyOrder("ABC - Copy", "ABC")
    }

    @Test
    fun `Should support renaming a template`() {
        val id = insertTemplateAndRule(name = "Old").rowId

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("rename")

        typeIntoInputDialog("Name", "New", title = "Rename Template")

        scrn.getTemplate(0).name shouldBe "New"

        val newEntity = DartzeeTemplateEntity().retrieveForId(id)
        newEntity!!.name shouldBe "New"
    }

    @Test
    fun `Should do nothing if rename is cancelled`() {
        insertTemplateAndRule(name = "ABC")

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("rename")

        dismissDialog("Rename Template")

        scrn.getTemplate(0).name shouldBe "ABC"
    }

    @Test
    fun `Should render the rules associated to the template`() {
        val template = insertDartzeeTemplate()

        val ruleOne = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(aggregateRule = DartzeeTotalRulePrime())

        ruleOne.toEntity(1, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()
        ruleTwo.toEntity(2, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        val rules = scrn.getChild<ScrollTable>().getValueAt(0, 1) as List<*>
        rules
            .map { (it as DartzeeRuleDto).generateRuleDescription() }
            .shouldContainExactly(
                ruleOne.generateRuleDescription(),
                ruleTwo.generateRuleDescription(),
            )
    }

    private fun DartzeeTemplateSetupScreen.getTemplate(row: Int) =
        getChild<ScrollTable>().getValueAt(row, 0) as DartzeeTemplateEntity

    private fun DartzeeTemplateSetupScreen.getGameCount(row: Int) =
        getChild<ScrollTable>().getValueAt(row, 2) as Int
}
