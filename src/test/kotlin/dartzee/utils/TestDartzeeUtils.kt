package dartzee.utils

import dartzee.`object`.Dart
import dartzee.db.DartzeeRuleEntity
import dartzee.helper.*
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test
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
        insertDartzeeRules(game, dtos)

        val entities = DartzeeRuleEntity().retrieveEntities("")

        entities.map { it.toDto().generateRuleDescription() }.shouldContainExactlyInAnyOrder(*dtoDescs.toTypedArray())
        entities.forEach {
            it.entityName shouldBe "Game"
            it.entityId shouldBe game.rowId
        }

        entities.filter { it.ordinal == 1 }.map { it.toDto().generateRuleDescription() }.shouldContainExactly(innerOuterInner.generateRuleDescription())
        entities.filter { it.ordinal == 2 }.map { it.toDto().generateRuleDescription() }.shouldContainExactly(twoBlackOneWhite.generateRuleDescription())
    }

    @Test
    fun `Should do nothing if empty list of rules is passed`()
    {
        val game = insertGame()
        insertDartzeeRules(game)

        getCountFromTable("DartzeeRule") shouldBe 0
    }
}