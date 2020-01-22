package dartzee.core.util

import dartzee.core.util.MathsUtil
import dartzee.core.util.ceilDiv
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

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
}