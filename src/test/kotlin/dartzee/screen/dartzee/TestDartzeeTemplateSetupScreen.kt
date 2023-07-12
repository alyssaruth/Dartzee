package dartzee.screen.dartzee

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import dartzee.core.bean.ScrollTable
import dartzee.core.helper.processKeyPress
import dartzee.dartzee.DartzeeRuleDto
import dartzee.dartzee.DartzeeTemplateFactory
import dartzee.dartzee.aggregate.DartzeeTotalRulePrime
import dartzee.dartzee.dart.DartzeeDartRuleEven
import dartzee.db.DartzeeTemplateEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.FakeDartzeeTemplateFactory
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDartzeeTemplate
import dartzee.helper.insertGame
import dartzee.helper.insertTemplateAndRule
import dartzee.helper.makeDartzeeRuleDto
import dartzee.utils.InjectedThings
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.awt.event.KeyEvent
import javax.swing.JButton
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

        scrn.getChild<JButton>("copy").shouldBeDisabled()
        scrn.getChild<JButton>("delete").shouldBeDisabled()
        scrn.getChild<JButton>("rename").shouldBeDisabled()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.getChild<JButton>("copy").shouldBeEnabled()
        scrn.getChild<JButton>("delete").shouldBeEnabled()
        scrn.getChild<JButton>("rename").shouldBeEnabled()

        scrn.getChild<ScrollTable>().selectRow(-1)
        scrn.getChild<JButton>("copy").shouldBeDisabled()
        scrn.getChild<JButton>("delete").shouldBeDisabled()
        scrn.getChild<JButton>("rename").shouldBeDisabled()
    }

    @Test
    fun `Should leave template alone if delete is cancelled`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.NO_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")
        scrn.getChild<ScrollTable>().rowCount shouldBe 1
        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 1
    }

    @Test
    fun `Should delete a template and associated rules on confirmation`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("delete")

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 0
        getCountFromTable(EntityName.DartzeeRule) shouldBe 0
    }

    @Test
    fun `Should support deleting by using the keyboard shortcut`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.getChild<ScrollTable>().processKeyPress(KeyEvent.VK_DELETE)

        dialogFactory.questionsShown.shouldContainExactly("Are you sure you want to delete the ABC Template?")

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 0
        getCountFromTable(EntityName.DartzeeRule) shouldBe 0
    }

    @Test
    fun `Pressing delete with no row selected should do nothing`()
    {
        insertTemplateAndRule(name = "ABC")
        dialogFactory.questionOption = JOptionPane.YES_OPTION

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(-1)
        scrn.getChild<ScrollTable>().processKeyPress(KeyEvent.VK_DELETE)

        dialogFactory.questionsShown.shouldBeEmpty()

        scrn.getChild<ScrollTable>().rowCount shouldBe 1
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

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("delete")

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
        scrn.clickChild<JButton>("add")

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
    }

    @Test
    fun `Should add a template to the table upon creation`()
    {
        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        val template = insertTemplateAndRule(name = "My Template")
        InjectedThings.dartzeeTemplateFactory = FakeDartzeeTemplateFactory(template)

        scrn.getChild<ScrollTable>().rowCount shouldBe 0
        scrn.clickChild<JButton>("add")
        scrn.getChild<ScrollTable>().rowCount shouldBe 1
        scrn.getTemplate(0).rowId shouldBe template.rowId
    }

    @Test
    fun `Should do nothing if template copying is cancelled`()
    {
        insertTemplateAndRule(name = "ABC")
        InjectedThings.dartzeeTemplateFactory = FakeDartzeeTemplateFactory(cancelCopy = true)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("copy")

        scrn.getChild<ScrollTable>().rowCount shouldBe 1
    }

    @Test
    fun `Should copy a template and add it to the table`()
    {
        insertTemplateAndRule(name = "ABC")
        InjectedThings.dartzeeTemplateFactory = FakeDartzeeTemplateFactory(cancelCopy = false)

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("copy")

        scrn.getChild<ScrollTable>().rowCount shouldBe 2
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

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("rename")

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

        scrn.getChild<ScrollTable>().selectRow(0)
        scrn.clickChild<JButton>("rename")

        dialogFactory.inputsShown.shouldContainExactly("Rename Template")
        scrn.getTemplate(0).name shouldBe "ABC"
    }

    @Test
    fun `Should render the rules associated to the template`()
    {
        val template = insertDartzeeTemplate()

        val ruleOne = makeDartzeeRuleDto(DartzeeDartRuleEven())
        val ruleTwo = makeDartzeeRuleDto(aggregateRule = DartzeeTotalRulePrime())

        ruleOne.toEntity(1, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()
        ruleTwo.toEntity(2, EntityName.DartzeeTemplate, template.rowId).saveToDatabase()

        val scrn = DartzeeTemplateSetupScreen()
        scrn.initialise()

        val rules = scrn.getChild<ScrollTable>().getValueAt(0, 1) as List<*>
        rules.map { (it as DartzeeRuleDto).generateRuleDescription() }.shouldContainExactly(ruleOne.generateRuleDescription(), ruleTwo.generateRuleDescription())
    }

    private fun DartzeeTemplateSetupScreen.getTemplate(row: Int): DartzeeTemplateEntity
    {
        return getChild<ScrollTable>().getValueAt(row, 0) as DartzeeTemplateEntity
    }

    private fun DartzeeTemplateSetupScreen.getGameCount(row: Int): Int
    {
        return getChild<ScrollTable>().getValueAt(row, 2) as Int
    }
}