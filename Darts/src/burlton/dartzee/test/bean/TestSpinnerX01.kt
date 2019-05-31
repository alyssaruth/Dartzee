package burlton.dartzee.test.bean

import burlton.dartzee.code.bean.SpinnerX01
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestSpinnerX01: AbstractDartsTest()
{
    @Test
    fun `It should default to 501`()
    {
        val spinner = SpinnerX01()
        spinner.value shouldBe 501
    }

    @Test
    fun `It should increment in multiples of 100`()
    {
        val spinner = SpinnerX01()

        //TODO ...
    }

}