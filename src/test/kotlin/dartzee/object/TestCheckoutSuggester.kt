package dartzee.`object`

import dartzee.helper.AbstractTest
import dartzee.utils.getCheckoutScores
import dartzee.utils.sumScore
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldHaveAtMostSize
import io.kotlintest.matchers.collections.shouldHaveSingleElement
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import org.junit.Test

class TestCheckoutSuggester: AbstractTest()
{
    @Test
    fun `should parse checkouts for one, two or three darts`()
    {
        val list = listOf("170=T20,T20,D25")

        val map = CheckoutSuggester.parseCheckouts(list)

        map.shouldContain("170-3", listOf(DartHint(20, 3), DartHint(20, 3), DartHint(25, 2)))
        map.shouldContain("110-2", listOf(DartHint(20, 3), DartHint(25, 2)))
        map.shouldContain("50-1", listOf(DartHint(25, 2)))
    }

    @Test
    fun `should throw an error if a checkout is invalid`()
    {
        val list = listOf("170=XXX")

        shouldThrow<NumberFormatException>{
            CheckoutSuggester.parseCheckouts(list)
        }
    }

    @Test
    fun `should return null if no checkout exists`()
    {
        CheckoutSuggester.suggestCheckout(180, 3) shouldBe null
        CheckoutSuggester.suggestCheckout(60, 1) shouldBe null
    }

    @Test
    fun `should only suggest legitimate checkouts`()
    {
        for (i in 0..171)
        {
            for (darts in 1..3)
            {
                val checkout = CheckoutSuggester.suggestCheckout(i, darts) ?: continue

                checkout.shouldHaveAtMostSize(darts)
                sumScore(checkout) shouldBe i
                checkout.last().isDouble().shouldBeTrue()
            }
        }
    }

    @Test
    fun `should have suggestions for everything up to 150`()
    {
        for (i in 2..150)
        {
            val checkout = CheckoutSuggester.suggestCheckout(i, 3)
            if (checkout == null)
            {
                println(i)
            }
            checkout shouldNotBe null
        }
    }

    @Test
    fun `should suggest straight doubles when you're on one`()
    {
        getCheckoutScores().forEach{
            val checkout = CheckoutSuggester.suggestCheckout(it, 3)!!
            checkout.shouldHaveSingleElement(DartHint(it/2, 2))
        }
    }
}
