package dartzee.core.util

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
        StringUtil.convertOrdinalToText(11) shouldBe "11th"
        StringUtil.convertOrdinalToText(12) shouldBe "12th"
        StringUtil.convertOrdinalToText(13) shouldBe "13th"
    }

    @Test
    fun `Should return the right position descriptions for random other numbers`()
    {
        StringUtil.convertOrdinalToText(20) shouldBe "20th"
        StringUtil.convertOrdinalToText(51) shouldBe "51st"
        StringUtil.convertOrdinalToText(102) shouldBe "102nd"
    }
}