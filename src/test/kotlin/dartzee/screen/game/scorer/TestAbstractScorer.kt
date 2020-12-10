package dartzee.screen.game.scorer

import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.shouldBeVisible
import dartzee.shouldNotBeVisible
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestAbstractScorer: AbstractTest()
{
    @Test
    fun `Should initialise correctly without a player`()
    {
        val scorer = TestScorer()
        scorer.init(null)

        scorer.lblName.shouldNotBeVisible()
        scorer.lblAvatar.shouldNotBeVisible()
        scorer.panelAvatar.shouldNotBeVisible()
        scorer.playerId shouldBe ""
    }

    @Test
    fun `Should initialise correctly with a player`()
    {
        val player = insertPlayer(name = "Bob")
        val scorer = TestScorer()
        scorer.init(player)

        scorer.lblName.shouldBeVisible()
        scorer.lblName.text shouldBe "Bob"
        scorer.lblAvatar.shouldBeVisible()
        scorer.lblAvatar.avatarId shouldBe player.playerImageId
        scorer.lblAvatar.icon shouldBe player.getAvatar()
        scorer.panelAvatar.shouldBeVisible()
        scorer.playerId shouldBe player.rowId
    }

    @Test
    fun `Should initialise with the right number of columns`()
    {
        val twoCols = TestScorer(2)
        twoCols.init(null)
        twoCols.tableScores.model.columnCount shouldBe 2

        val fourCols = TestScorer(4)
        fourCols.init(null)
        fourCols.tableScores.model.columnCount shouldBe 4
    }

    @Test
    fun `Should call init implementation`()
    {
        val scorer = TestScorer()
        scorer.initted shouldBe false
        scorer.init(null)
        scorer.initted shouldBe true
    }

    @Test
    fun `Should only be assignable if visible and not already assigned`()
    {
        val scorer = TestScorer()
        scorer.isVisible = false
        scorer.canBeAssigned() shouldBe false

        scorer.isVisible = true
        scorer.canBeAssigned() shouldBe true

        scorer.init(insertPlayer())
        scorer.canBeAssigned() shouldBe false
    }

    private class TestScorer(val columnCount: Int = 4): AbstractScorer()
    {
        var initted = false

        override fun getNumberOfColumns() = columnCount

        override fun initImpl()
        {
            initted = true
        }

    }
}