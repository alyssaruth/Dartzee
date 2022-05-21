package dartzee.screen.game

import dartzee.game.state.IWrappedParticipant
import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.game.scorer.AbstractScorer
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestPanelWithScorers: AbstractTest()
{
    @Test
    fun `Should init with the right number of scorers, and split them evenly between east and west`()
    {
        val scrn = FakeDartsScreen()

        scrn.assignScorers(5)
        scrn.finaliseScorers()

        scrn.scorerCount() shouldBe 5
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0), scrn.getScorer(1), scrn.getScorer(2))
        scrn.getEastScorers().shouldContainExactly(scrn.getScorer(3), scrn.getScorer(4))

        val scrn4 = FakeDartsScreen()
        scrn4.assignScorers(4)
        scrn4.finaliseScorers()
        scrn4.scorerCount() shouldBe 4
        scrn4.getWestScorers().shouldContainExactly(scrn.getScorer(0), scrn.getScorer(1))
        scrn4.getEastScorers().shouldContainExactly(scrn.getScorer(2), scrn.getScorer(3))
    }

    @Test
    fun `Should handle just a single scorer`()
    {
        val scrn = FakeDartsScreen()

        scrn.assignScorer(makeSingleParticipant())
        scrn.finaliseScorers()

        scrn.scorerCount() shouldBe 1
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0))
        scrn.getEastScorers().shouldBeEmpty()
    }

    @Test
    fun `Should assign scorers in order`()
    {
        val scrn = FakeDartsScreen()

        scrn.assignScorer(makeSingleParticipant(insertPlayer(name = "Player One")))
        scrn.assignScorer(makeSingleParticipant(insertPlayer(name = "Player Two")))
        scrn.assignScorer(makeSingleParticipant(insertPlayer(name = "Player Three")))

        scrn.getScorer(0).lblName.text shouldBe "Player One"
        scrn.getScorer(1).lblName.text shouldBe "Player Two"
        scrn.getScorer(2).lblName.text shouldBe "Player Three"
    }

    inner class FakeScorer(participant: IWrappedParticipant) : AbstractScorer(participant)
    {
        override fun getNumberOfColumns() = 4
        override fun initImpl() {}
    }

    inner class FakeDartsScreen: PanelWithScorers<FakeScorer>()
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