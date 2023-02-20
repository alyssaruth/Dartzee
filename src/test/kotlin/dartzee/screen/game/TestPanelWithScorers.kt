package dartzee.screen.game

import dartzee.game.state.IWrappedParticipant
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.game.scorer.AbstractScorer
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class TestPanelWithScorers: AbstractTest()
{
    @Test
    fun `Assigning a scorer should initialise it`()
    {
        val scrn = FakePanelWithScorers()

        val singleParticipant = makeSingleParticipant()
        scrn.assignScorer(singleParticipant)

        val scorer = scrn.getScorer(0)
        scorer.initted shouldBe true
    }

    @Test
    fun `Should split 5 scorers correctly`()
    {
        val scrn = FakePanelWithScorers()

        scrn.assignScorers(5)
        scrn.finaliseScorers(FakeDartsScreen())

        scrn.scorerCount() shouldBe 5
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0), scrn.getScorer(1), scrn.getScorer(2))
        scrn.getEastScorers().shouldContainExactly(scrn.getScorer(3), scrn.getScorer(4))
    }

    @Test
    fun `Should split 4 scorers correctly`()
    {
        val scrn = FakePanelWithScorers()

        scrn.assignScorers(4)
        scrn.finaliseScorers(FakeDartsScreen())

        scrn.scorerCount() shouldBe 4
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0), scrn.getScorer(1))
        scrn.getEastScorers().shouldContainExactly(scrn.getScorer(2), scrn.getScorer(3))
    }

    @Test
    fun `Should handle just a single scorer`()
    {
        val scrn = FakePanelWithScorers()

        scrn.assignScorer(makeSingleParticipant())
        scrn.finaliseScorers(FakeDartsScreen())

        scrn.scorerCount() shouldBe 1
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0))
        scrn.getEastScorers().shouldBeEmpty()
    }

    @Test
    fun `Should assign scorers in order`()
    {
        val scrn = FakePanelWithScorers()

        scrn.assignScorer(makeSingleParticipant(insertPlayer(name = "Player One")))
        scrn.assignScorer(makeSingleParticipant(insertPlayer(name = "Player Two")))
        scrn.assignScorer(makeSingleParticipant(insertPlayer(name = "Player Three")))

        scrn.getScorer(0).lblName.text shouldContain "Player One"
        scrn.getScorer(1).lblName.text shouldContain "Player Two"
        scrn.getScorer(2).lblName.text shouldContain "Player Three"
    }

    inner class FakeScorer(participant: IWrappedParticipant) : AbstractScorer(participant)
    {
        var initted = false

        override fun getNumberOfColumns() = 4
        override fun initImpl() {
            initted = true
        }
    }

    inner class FakePanelWithScorers : PanelWithScorers<FakeScorer>()
    {
        override fun factoryScorer(participant: IWrappedParticipant) = FakeScorer(participant)

        fun assignScorers(scorerCount: Int) {
            repeat(scorerCount) { assignScorer(makeSingleParticipant()) }
        }

        fun scorerCount() = scorersOrdered.size
        fun getScorer(i: Int) = scorersOrdered[i]

        fun getEastScorers() = panelEast.components.toList()
        fun getWestScorers() = panelWest.components.toList()
    }
}