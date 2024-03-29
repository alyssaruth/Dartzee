package dartzee.`object`

import dartzee.helper.AbstractTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDartHint : AbstractTest() {
    @Test
    fun `should factory doubles`() {
        val dart = factoryDartHintFromString("D20")

        dart shouldBe DartHint(20, 2)
    }

    @Test
    fun `should factory trebles`() {
        val dart = factoryDartHintFromString("T15")

        dart shouldBe DartHint(15, 3)
    }

    @Test
    fun `should factory singles`() {
        val dart = factoryDartHintFromString("18")

        dart shouldBe DartHint(18, 1)
    }

    @Test
    fun `should throw an error for invalid input`() {
        shouldThrow<NumberFormatException> { factoryDartHintFromString("X20") }
    }
}
