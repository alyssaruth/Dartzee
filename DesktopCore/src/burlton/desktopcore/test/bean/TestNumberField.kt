package burlton.desktopcore.test.bean

import burlton.dartzee.code.core.bean.NumberField
import burlton.desktopcore.test.helper.AbstractTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import java.text.ParseException

class TestNumberField: AbstractTest()
{
    @Test
    fun `Should return -1 when value unset`()
    {
        val nf = NumberField()

        nf.getNumber() shouldBe -1
    }

    @Test
    fun `Should be unbounded by default`()
    {
        val nf = NumberField()

        nf.formatter.stringToValue("${Integer.MAX_VALUE}")
        nf.formatter.stringToValue("${Integer.MIN_VALUE}")
    }

    @Test
    fun `Should allow negative values`()
    {
        val nf = NumberField()

        nf.formatter.stringToValue("-20")
    }

    @Test
    fun `Shouldn't allow non-numerics`()
    {
        val nf = NumberField()

        shouldThrow<ParseException>{
            nf.formatter.stringToValue("x")
        }
    }

    @Test
    fun `Should respect min and max values from constructor`()
    {
        val nf = NumberField(10, 20)

        shouldThrow<ParseException>{
            nf.formatter.stringToValue("21")
        }

        shouldThrow<ParseException>{
            nf.formatter.stringToValue("9")
        }

        //Shouldn't throw errors
        nf.formatter.stringToValue("10")
        nf.formatter.stringToValue("20")
    }
}