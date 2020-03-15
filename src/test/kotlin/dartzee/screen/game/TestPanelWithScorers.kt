package dartzee.screen.game

import dartzee.helper.AbstractTest
import dartzee.helper.insertPlayer
import dartzee.screen.game.scorer.AbstractScorer
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import org.junit.Test

class TestPanelWithScorers: AbstractTest()
{
    @Test
    fun `Should init with the right number of scorers, and split them evenly between east and west`()
    {
        val scrn = FakeDartsScreen()

        scrn.initScorers(5)

        scrn.scorerCount() shouldBe 5
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0), scrn.getScorer(1), scrn.getScorer(2))
        scrn.getEastScorers().shouldContainExactly(scrn.getScorer(3), scrn.getScorer(4))

        scrn.initScorers(4)
        scrn.scorerCount() shouldBe 4
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0), scrn.getScorer(1))
        scrn.getEastScorers().shouldContainExactly(scrn.getScorer(2), scrn.getScorer(3))
    }

    @Test
    fun `Should handle just a single scorer`()
    {
        val scrn = FakeDartsScreen()

        scrn.initScorers(1)

        scrn.scorerCount() shouldBe 1
        scrn.getWestScorers().shouldContainExactly(scrn.getScorer(0))
        scrn.getEastScorers().shouldBeEmpty()
    }

    @Test
    fun `Should throw an exception if there are no remaining scorers to be assigned`()
    {
        val scrn = FakeDartsScreen()
        scrn.initScorers(2)

        scrn.assignScorer(insertPlayer(), "")
        scrn.assignScorer(insertPlayer(), "")

        val e = shouldThrowExactly<Exception> {
            scrn.assignScorer(insertPlayer(name = "Richard"), "")
        }

        e.message shouldBe "Unable to assign scorer for player Richard"
    }

    @Test
    fun `Should assign scorers in order`()
    {
        val scrn = FakeDartsScreen()
        scrn.initScorers(3)

        scrn.assignScorer(insertPlayer(name = "Player One"), "")
        scrn.assignScorer(insertPlayer(name = "Player Two"), "")
        scrn.assignScorer(insertPlayer(name = "Player Three"), "")

        scrn.getScorer(0).lblName.text shouldBe "Player One"
        scrn.getScorer(1).lblName.text shouldBe "Player Two"
        scrn.getScorer(2).lblName.text shouldBe "Player Three"
    }


    inner class FakeScorer: AbstractScorer()
    {
        override fun getNumberOfColumns() = 4
        override fun initImpl(gameParams: String) {}
    }

    inner class FakeDartsScreen: PanelWithScorers<FakeScorer>()
    {
        override fun factoryScorer() = FakeScorer()

        fun scorerCount() = scorersOrdered.size
        fun getScorer(i: Int) = scorersOrdered[i]

        fun getEastScorers() = panelEast.components.toList()
        fun getWestScorers() = panelWest.components.toList()
    }
}