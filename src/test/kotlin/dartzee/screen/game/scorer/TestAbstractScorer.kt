package dartzee.screen.game.scorer

import com.github.alexburlton.swingtest.shouldBeVisible
import dartzee.game.state.IWrappedParticipant
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.game.makeSingleParticipant
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class TestAbstractScorer: AbstractTest()
{
    @Test
    fun `Should initialise correctly with a single participant`()
    {
        val player = insertPlayer(name = "Bob")
        val participant = makeSingleParticipant(player)
        val scorer = TestScorer(participant)
        scorer.init()

        scorer.lblName.text shouldContain "Bob"
        scorer.lblAvatar.icon.shouldNotBeNull()
        scorer.panelAvatar.shouldBeVisible()
        scorer.playerIds.shouldContainExactly(player.rowId)
    }

    @Test
    fun `Should initialise with the right number of columns`()
    {
        val twoCols = TestScorer(columnCount =  2)
        twoCols.init()
        twoCols.tableScores.model.columnCount shouldBe 2

        val fourCols = TestScorer(columnCount = 4)
        fourCols.init()
        fourCols.tableScores.model.columnCount shouldBe 4
    }

    @Test
    fun `Should call init implementation`()
    {
        val scorer = TestScorer()
        scorer.initted shouldBe false
        scorer.init()
        scorer.initted shouldBe true
    }

    private class TestScorer(participant: IWrappedParticipant = makeSingleParticipant(), val columnCount: Int = 4): AbstractScorer(participant)
    {
        var initted = false

        override fun getNumberOfColumns() = columnCount

        override fun initImpl()
        {
            initted = true
        }

    }
}