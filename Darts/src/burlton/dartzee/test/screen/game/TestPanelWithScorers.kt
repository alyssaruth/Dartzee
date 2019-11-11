package burlton.dartzee.test.screen.game

import burlton.dartzee.code.screen.game.scorer.AbstractScorer
import burlton.dartzee.code.screen.game.PanelWithScorers
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertPlayer
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import org.junit.Test

class TestPanelWithScorers: AbstractDartsTest()
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
    fun `Should throw an exception if there are no remaining scorers to be assigned`()
    {
        val scrn = FakeDartsScreen()
        scrn.initScorers(2)

        val hmPlayerToScorer = mutableMapOf<Int, FakeScorer>()
        scrn.assignScorer(insertPlayer(), hmPlayerToScorer, 0, "")
        scrn.assignScorer(insertPlayer(), hmPlayerToScorer, 1, "")

        val e = shouldThrowExactly<Exception> {
            scrn.assignScorer(insertPlayer(name = "Richard"), hmPlayerToScorer, 2, "")
        }

        e.message shouldBe "Unable to assign scorer for player Richard and key 2"
    }

    @Test
    fun `Should assign scorers in order and correctly populate the hash map`()
    {
        val scrn = FakeDartsScreen()
        scrn.initScorers(3)

        val hmPlayerToScorer = mutableMapOf<Int, FakeScorer>()

        scrn.assignScorer(insertPlayer(name = "Player One"), hmPlayerToScorer, 0, "")
        scrn.assignScorer(insertPlayer(name = "Player Two"), hmPlayerToScorer, 1, "")
        scrn.assignScorer(insertPlayer(name = "Player Three"), hmPlayerToScorer, 2, "")

        hmPlayerToScorer[0] shouldBe scrn.getScorer(0)
        hmPlayerToScorer[1] shouldBe scrn.getScorer(1)
        hmPlayerToScorer[2] shouldBe scrn.getScorer(2)

        scrn.getScorer(0).lblName.text shouldBe "Player One"
        scrn.getScorer(1).lblName.text shouldBe "Player Two"
        scrn.getScorer(2).lblName.text shouldBe "Player Three"
    }


    inner class FakeScorer: AbstractScorer()
    {
        override fun getNumberOfColumns() = 4
        override fun initImpl(gameParams: String?) {}
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