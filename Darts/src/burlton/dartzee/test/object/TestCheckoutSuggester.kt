package burlton.dartzee.test.`object`

import burlton.dartzee.code.`object`.CheckoutSuggester
import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.utils.sumScore
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class TestCheckoutSuggester
{
    @Test
    fun `should parse checkouts for one, two or three darts`()
    {
        val list = listOf("170=T20,T20,D25", "60=20,D20", "20=D10")

        val map = CheckoutSuggester.parseCheckouts(list)

        map.shouldContain(170, listOf(Dart(20, 3), Dart(20, 3), Dart(25, 2)))
        map.shouldContain(60, listOf(Dart(20, 1), Dart(20, 2)))
        map.shouldContain(20, listOf(Dart(10, 2)))
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
        CheckoutSuggester.suggestCheckout(180) shouldBe null
    }

    @Test
    fun `should only suggest legitimate checkouts`()
    {
        for (i in 59..171)
        {
            val checkout = CheckoutSuggester.suggestCheckout(i) ?: continue

            sumScore(checkout) shouldBe i
            checkout.last().isDouble().shouldBeTrue()
        }
    }
}
