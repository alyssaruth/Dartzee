package dartzee.utils

import dartzee.db.DartzeeRuleEntity
import dartzee.db.EntityName
import dartzee.db.GameEntity
import dartzee.game.GameType
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.innerOuterInner
import dartzee.helper.insertGame
import dartzee.helper.twoBlackOneWhite
import dartzee.`object`.Dart
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color
import javax.swing.JButton

class TestDartzeeUtils: AbstractTest()
{
    @Test
    fun `Should set the right colours based on dartzee result`()
    {
        val c = JButton()

        c.setColoursForDartzeeResult(true)
        c.background shouldBe Color.GREEN
        c.foreground shouldBe DartsColour.getProportionalColourRedToGreen(1.0, 1, 0.5)

        c.setColoursForDartzeeResult(false)
        c.background shouldBe Color.RED
        c.foreground shouldBe DartsColour.getProportionalColourRedToGreen(0.0, 1, 0.5)
    }

    @Test
    fun `Should create a high score round result`()
    {
        val darts = listOf(Dart(20, 1), Dart(15, 2), Dart(3, 2))
        val rr = factoryHighScoreResult(darts)

        rr.ruleNumber shouldBe -1
        rr.success shouldBe true
        rr.score shouldBe 56
    }

    @Test
    fun `Should insert dartzee rules for a game`()
    {
        val dtos = listOf(innerOuterInner, twoBlackOneWhite)
        val dtoDescs = dtos.map { it.generateRuleDescription() }

        val game = insertGame()
        insertDartzeeRules(game.rowId, dtos)

        val entities = DartzeeRuleEntity().retrieveEntities("")

        entities.map { it.toDto().generateRuleDescription() }.shouldContainExactlyInAnyOrder(*dtoDescs.toTypedArray())
        entities.forEach {
            it.entityName shouldBe EntityName.Game
            it.entityId shouldBe game.rowId
        }

        entities.filter { it.ordinal == 1 }.map { it.toDto().generateRuleDescription() }.shouldContainExactly(innerOuterInner.generateRuleDescription())
        entities.filter { it.ordinal == 2 }.map { it.toDto().generateRuleDescription() }.shouldContainExactly(twoBlackOneWhite.generateRuleDescription())
    }

    @Test
    fun `Should do nothing if empty list of rules is passed`()
    {
        val game = insertGame()
        insertDartzeeRules(game.rowId)

        getCountFromTable("DartzeeRule") shouldBe 0
    }

    @Test
    fun `Should save a dartzee template`()
    {
        val dtos = listOf(innerOuterInner, twoBlackOneWhite)
        val template = saveDartzeeTemplate("Template", dtos)

        val retrievedRuleDescriptions = DartzeeRuleEntity().retrieveForTemplate(template.rowId).map { it.toDto().generateRuleDescription() }
        retrievedRuleDescriptions.shouldContainExactlyInAnyOrder(dtos.map { it.generateRuleDescription() })
    }

    @Test
    fun `Should return null when generating a template if no name is entered`()
    {
        dialogFactory.inputSelection = null

        val result = generateDartzeeTemplateFromGame(insertGame(), listOf())
        result shouldBe null
        getCountFromTable(EntityName.DartzeeTemplate) shouldBe 0
    }

    @Test
    fun `Should generate a template and update the game to point at it`()
    {
        dialogFactory.inputSelection = "My Template"

        val g = insertGame(gameType = GameType.DARTZEE, gameParams = "")
        val dtos = listOf(innerOuterInner, twoBlackOneWhite)
        val result = generateDartzeeTemplateFromGame(g, dtos)!!
        result.name shouldBe "My Template"

        val retrievedGame = GameEntity().retrieveForId(g.rowId)!!
        retrievedGame.gameParams shouldBe result.rowId

        val retrievedRuleDescriptions = DartzeeRuleEntity().retrieveForTemplate(result.rowId).map { it.toDto().generateRuleDescription() }
        retrievedRuleDescriptions.shouldContainExactlyInAnyOrder(dtos.map { it.generateRuleDescription() })
        dialogFactory.infosShown.shouldContainExactly("Template 'My Template' successfully created.")
    }
}