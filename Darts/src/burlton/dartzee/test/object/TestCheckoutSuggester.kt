package burlton.dartzee.test.`object`

import burlton.dartzee.code.`object`.CheckoutSuggester
import burlton.dartzee.code.`object`.Dart
import io.kotlintest.matchers.maps.shouldContain
import org.junit.Test

class TestCheckoutSuggester
{
    @Test
    fun `it should parse checkouts for one, two or three darts`()
    {
        val list = listOf("170=T20,T20,D25", "60=20,D20", "20=D10")

        val map = CheckoutSuggester.parseCheckouts(list)

        map.shouldContain(170, listOf(Dart(20, 3), Dart(20, 3), Dart(25, 2)))
        map.shouldContain(60, listOf(Dart(20, 1), Dart(20, 2)))
        map.shouldContain(20, listOf(Dart(10, 2)))
    }
}
