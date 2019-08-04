package burlton.core.test.util

import burlton.core.code.util.addUnique
import burlton.core.code.util.getDescription
import io.kotlintest.matchers.collections.shouldContainExactly
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

    @Test
    fun `Should only add unique elements`()
    {
        val list = mutableListOf<String>()

        list.addUnique("foo")
        list.shouldContainExactly("foo")

        list.addUnique("foo")
        list.shouldContainExactly("foo")

        list.addUnique("bar")
        list.shouldContainExactly("foo", "bar")
    }
}