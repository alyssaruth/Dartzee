package dartzee.core.util

import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestMathsUtil: AbstractTest()
{
    @Test
    fun `Should round to the specified dp`()
    {
        val number = 20.547

        MathsUtil.round(number, 0) shouldBe 21.0
        MathsUtil.round(number, 1) shouldBe 20.5
        MathsUtil.round(number, 2) shouldBe 20.55
        MathsUtil.round(number, 3) shouldBe 20.547
    }

    @Test
    fun `Should get percentages to 1 dp`()
    {
        val total = 10000.0

        MathsUtil.getPercentage(1, total) shouldBe 0.0
        MathsUtil.getPercentage(5, total) shouldBe 0.1
        MathsUtil.getPercentage(55, total) shouldBe 0.6
        MathsUtil.getPercentage(673, total) shouldBe 6.7
        MathsUtil.getPercentage(678, total) shouldBe 6.8
        MathsUtil.getPercentage(9994, total) shouldBe 99.9
        MathsUtil.getPercentage(9999, total) shouldBe 100.0
        MathsUtil.getPercentage(10000, total) shouldBe 100.0
    }

    @Test
    fun `Percentages should still work for large numbers`()
    {
        val total = 123456788.0

        MathsUtil.getPercentage(123456788, total) shouldBe 100.0
        MathsUtil.getPercentage(61728394, total) shouldBe 50.0
    }

    @Test
    fun `Percentage for thirds`()
    {
        val total = 3.0

        MathsUtil.getPercentage(0, total) shouldBe 0.0
        MathsUtil.getPercentage(1, total) shouldBe 33.3
        MathsUtil.getPercentage(2, total) shouldBe 66.7
    }

    @Test
    fun `Test ceiling divide`()
    {
        4.ceilDiv(2) shouldBe 2
        7.ceilDiv(3) shouldBe 3
        5.ceilDiv(2) shouldBe 3
        5.ceilDiv(5) shouldBe 1
    }

    @Test
    fun `mapStepped should not run into any stupid double precision nonsense`() {
        val list = Pair(-5.0, 5.0).mapStepped(0.1) { it }
        list.shouldContainExactly(
            -5.0, -4.9, -4.8, -4.7, -4.6, -4.5, -4.4, -4.3, -4.2, -4.1,
            -4.0, -3.9, -3.8, -3.7, -3.6, -3.5, -3.4, -3.3, -3.2, -3.1,
            -3.0, -2.9, -2.8, -2.7, -2.6, -2.5, -2.4, -2.3, -2.2, -2.1,
            -2.0, -1.9, -1.8, -1.7, -1.6, -1.5, -1.4, -1.3, -1.2, -1.1,
            -1.0, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.3, -0.2, -0.1,
            0.0,
            0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0,
            1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0,
            2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 3.0,
            3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 4.0,
            4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9
        )
    }
}