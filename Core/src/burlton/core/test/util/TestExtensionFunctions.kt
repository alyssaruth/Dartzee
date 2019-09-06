package burlton.core.test.util

import burlton.core.code.util.addUnique
import burlton.core.code.util.getAllPermutations
import burlton.core.code.util.getDescription
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
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

    @Test
    fun `Should return empty when permuting an empty list`()
    {
        val list = listOf<Any>()
        list.getAllPermutations().size shouldBe 1
        list.getAllPermutations()[0] shouldBe listOf()
    }

    @Test
    fun `Should generate all permutations of a list`()
    {
        val list = listOf(1, 2, 3)

        list.getAllPermutations().shouldContainExactlyInAnyOrder(listOf(1, 2, 3), listOf(1, 3, 2), listOf(2, 1, 3), listOf(2, 3, 1), listOf(3, 1, 2), listOf(3, 2, 1))
    }
}