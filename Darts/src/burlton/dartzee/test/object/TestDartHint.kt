package burlton.dartzee.test.`object`

import burlton.dartzee.code.`object`.DartHint
import burlton.dartzee.code.`object`.factoryDartHintFromString
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class TestDartHint: AbstractDartsTest()
{
    @Test
    fun `should factory doubles`()
    {
        val dart = factoryDartHintFromString("D20")

        dart shouldBe DartHint(20, 2)
    }

    @Test
    fun `should factory trebles`()
    {
        val dart = factoryDartHintFromString("T15")

        dart shouldBe DartHint(15, 3)
    }

    @Test
    fun `should factory singles`()
    {
        val dart = factoryDartHintFromString("18")

        dart shouldBe DartHint(18, 1)
    }

    @Test
    fun `should throw an error for invalid input`()
    {
        shouldThrow<NumberFormatException>{
            factoryDartHintFromString("X20")
        }
    }
}