package dartzee.screen.dartzee

import dartzee.core.helper.processKeyPress
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.DartzeeTemplateFactory
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.dartzee.total.DartzeeTotalRulePrime
import dartzee.db.DARTZEE_TEMPLATE
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.helper.*
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.awt.event.KeyEvent
import javax.swing.JOptionPane

class TestDartzeeTemplateSetupScreen: AbstractTest()
{
    @AfterEach
    fun afterEach()
    {
        InjectedThings.dartzeeTemplateFactory = DartzeeTemplateFactory()
    }

    @Test
    fun `Should pull through a game count of 0 for templates where no games have been played`()
    {
        insertTemplateAndRule()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getGameCount(0) shouldBe 0
    }

    @Test
    fun `Should pull through the right game count for templates where games have been played`()
    {
        val templateId = insertTemplateAndRule().rowId

        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)
        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getGameCount(0) shouldBe 2
    }

    @Test
    fun `Should toggle copy, remove and rename buttons based on whether a row is selected`()
    {
        insertTemplateAndRule()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.btnCopy.isEnabled shouldBe false
        scrn.btnDelete.isEnabled shouldBe false
        scrn.btnRename.isEnabled shouldBe false

        scrn.scrollTable.selectRow(0)
        scrn.btnCopy.isEnabled shouldBe true
        scrn.btnDelete.isEnabled shouldBe true
        scrn.btnRename.isEnabled shouldBe true

        scrn.scrollTable.selectRow(-1)
        scrn.btnCopy.isEnabled shouldBe false
        scrn.btnDelete.isEnabled shouldBe false
        scrn.btnRename.isEnabled shouldBe false
    }

    @Test
    fun `Should leave template alone if delete is cancelled`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnDelete.doClick()

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")
        scrn.scrollTable.rowCount shouldBe 1
        getCountFromTable(DARTZEE_TEMPLATE) shouldBe 1
    }

    @Test
    fun `Should delete a template and associated rules on confirmation`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnDelete.doClick()

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")

        scrn.scrollTable.rowCount shouldBe 0
        getCountFromTable(DARTZEE_TEMPLATE) shouldBe 0
        getCountFromTable("DartzeeRule") shouldBe 0
    }

    @Test
    fun `Should support deleting by using the keyboard shortcut`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.scrollTable.processKeyPress(KeyEvent.VK_DELETE)

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")

        scrn.scrollTable.rowCount shouldBe 0
        getCountFromTable(DARTZEE_TEMPLATE) shouldBe 0
        getCountFromTable("DartzeeRule") shouldBe 0
    }

    @Test
    fun `Pressing delete with no row selected should do nothing`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(-1)
        scrn.scrollTable.processKeyPress(KeyEvent.VK_DELETE)

        dialogFactory.questionsShown.shouldBeEmpty()

        scrn.scrollTable.rowCount shouldBe 1
    }

    @Test
    fun `Should revert games to Custom on deletion and show a different confirmation message`()
    {
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val templateId = insertTemplateAndRule(name = "ABC").rowId

        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)
        insertGame(gameType = GameType.DARTZEE, gameParams = templateId)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnDelete.doClick()

        dialogFactory.questionsShown.shouldContainExactly("You have played 2 games using the ABC Template." +
                "\n\nThese will become custom games if you delete it. Are you sure you want to continue?")

        GameEntity().retrieveEntities().forEach { it.gameParams shouldBe "" }
    }

    @Test
    fun `Should not add a template if cancelled`()
    {
        InjectedThings.dartzeeTemplateFactory = FakeDartzeeTemplateFactory(null)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()
        scrn.btnAdd.doClick()

        scrn.scrollTable.rowCount shouldBe 0
    }

    @Test
    fun `Should add a template to the table upon creation`()
    {
        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        val template = insertTemplateAndRule(name = "My Template")
        InjectedThings.dartzeeTemplateFactory = FakeDartzeeTemplateFactory(template)

        scrn.scrollTable.rowCount shouldBe 0
        scrn.btnAdd.doClick()
        scrn.scrollTable.rowCount shouldBe 1
        scrn.getTemplate(0).rowId shouldBe template.rowId
    }

    @Test
    fun `Should do nothing if template copying is cancelled`()
    {
        insertTemplateAndRule(name = "ABC")
        InjectedThings.dartzeeTemplateFactory = FakeDartzeeTemplateFactory(cancelCopy = true)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnCopy.doClick()

        scrn.scrollTable.rowCount shouldBe 1
    }

    @Test
    fun `Should copy a template and add it to the table`()
    {
        insertTemplateAndRule(name = "ABC")
        InjectedThings.dartzeeTemplateFactory = FakeDartzeeTemplateFactory(cancelCopy = false)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnCopy.doClick()

        scrn.scrollTable.rowCount shouldBe 2
        val templates = listOf(scrn.getTemplate(0), scrn.getTemplate(1)).map { it.name }
        templates.shouldContainExactlyInAnyOrder("ABC - Copy", "ABC")
    }

    @Test
    fun `Should support renaming a template`()
    {
        val id = insertTemplateAndRule(name = "Old").rowId

        dialogFactory.inputSelection = "New"

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnRename.doClick()

        dialogFactory.inputsShown.shouldContainExactly("Rename Template")
        scrn.getTemplate(0).name shouldBe "New"

        val newEntity = DartzeeTemplateEntity().retrieveForId(id)
        newEntity!!.name shouldBe "New"
    }

    @Test
    fun `Should do nothing if rename is cancelled`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.inputSelection = null

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.scrollTable.selectRow(0)
        scrn.btnRename.doClick()

        dialogFactory.inputsShown.shouldContainExactly("Rename Template")
        scrn.getTemplate(0).name shouldBe "ABC"
    }

    @Test
    fun `Should render the rules associated to the template`()
    {
        val template = insertDartzeeTemplate()

        val ruleOne = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(totalRule = DartzeeTotalRulePrime())

        ruleOne.toEntity(1, DARTZEE_TEMPLATE, template.rowId).saveToDatabase()
        ruleTwo.toEntity(2, DARTZEE_TEMPLATE, template.rowId).saveToDatabase()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        val rules = scrn.scrollTable.getValueAt(0, 1) as List<*>
        rules.map { (it as DartzeeRuleDto).generateRuleDescription() }.shouldContainExactly(ruleOne.generateRuleDescription(), ruleTwo.generateRuleDescription())
    }

    private fun DartzeeTemplateSetupScreen.getTemplate(row: Int): DartzeeTemplateEntity
    {
        return scrollTable.getValueAt(row, 0) as DartzeeTemplateEntity
    }

    private fun DartzeeTemplateSetupScreen.getGameCount(row: Int): Int
    {
        return scrollTable.getValueAt(row, 2) as Int
    }
}