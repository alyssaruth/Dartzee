package burlton.core.test.util

import burlton.core.code.util.getDescription
import io.kotlintest.shouldBe
import org.junit.Test

class TestExtensionFunctions
{
    @Test
    fun `IntRange descriptions`()
    {
        (1..1).getDescription() shouldBe "1"
        (1..3).getDescription() shouldBe "1 - 3"
        (1..Int.MAX_VALUE).getDescription() shouldBe "1+"
    }
}