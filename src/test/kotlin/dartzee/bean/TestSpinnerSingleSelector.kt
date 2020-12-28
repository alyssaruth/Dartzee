package dartzee.bean

import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestSpinnerSingleSelector: AbstractTest()
{
    @Test
    fun `Should not be able to select 21-24`()
    {
        val spinner = SpinnerSingleSelector()

        spinner.value = 21
        spinner.value shouldBe 25

        spinner.value = 22
        spinner.value shouldBe 25

        spinner.value = 23
        spinner.value shouldBe 20

        spinner.value = 24
        spinner.value shouldBe 20
    }
}