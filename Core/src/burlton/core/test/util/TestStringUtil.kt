package burlton.core.test.util

import burlton.core.code.util.StringUtil
import burlton.core.test.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestStringUtil: AbstractTest()
{
    @Test
    fun `Should return the right position descriptions for 1 - 13`()
    {
        StringUtil.convertOrdinalToText(1) shouldBe "1st"
        StringUtil.convertOrdinalToText(2) shouldBe "2nd"
        StringUtil.convertOrdinalToText(3) shouldBe "3rd"
        StringUtil.convertOrdinalToText(4) shouldBe "4th"
        StringUtil.convertOrdinalToText(5) shouldBe "5th"
        StringUtil.convertOrdinalToText(6) shouldBe "6th"
        StringUtil.convertOrdinalToText(7) shouldBe "7th"
        StringUtil.convertOrdinalToText(8) shouldBe "8th"
        StringUtil.convertOrdinalToText(9) shouldBe "9th"
        StringUtil.convertOrdinalToText(10) shouldBe "10th"
        StringUtil.convertOrdinalToText(10) shouldBe "11th"
        StringUtil.convertOrdinalToText(10) shouldBe "12th"
        StringUtil.convertOrdinalToText(10) shouldBe "13th"
    }

    @Test
    fun `Should return the right position descriptions for random other numbers`()
    {

    }
}