package dartzee.bean

import dartzee.helper.AbstractTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestSpinnerSingleSelector: AbstractTest()
{
    @Test
    fun `Should not be able to select 21-24`()
    {
        val spinner = SpinnerSingleSelector()

        spinner.value = 21
        assertEquals(spinner.value, 25)

        spinner.value = 22
        assertEquals(spinner.value, 25)

        spinner.value = 23
        assertEquals(spinner.value, 20)

        spinner.value = 24
        assertEquals(spinner.value, 20)
    }
}