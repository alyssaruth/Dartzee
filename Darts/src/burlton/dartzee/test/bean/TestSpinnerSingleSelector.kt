package burlton.dartzee.test.bean

import burlton.dartzee.code.bean.SpinnerSingleSelector
import org.junit.Test
import kotlin.test.assertEquals

class TestSpinnerSingleSelector
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