package dartzee.core.util

import dartzee.core.util.*
import dartzee.helper.AbstractTest
import dartzee.core.helper.verifyNotCalled
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class TestExtensionFunctions: AbstractTest()
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

    @Test
    fun `Should not return duplicate permutations`()
    {
        val list = listOf(1, 1, 2)

        list.getAllPermutations().shouldContainExactlyInAnyOrder(listOf(1, 1, 2), listOf(1, 2, 1), listOf(2, 1, 1))
    }

    class AlwaysValid
    {
        fun isValid(text: String, index: Int) = true
    }
    @Test
    fun `Should pass the element & index to the predicate correctly`()
    {
        val mockValidator = spyk<AlwaysValid>()

        val list = listOf("one", "two", "three", "four")

        list.allIndexed { index, value -> mockValidator.isValid(value, index) }

        verify { mockValidator.isValid("one", 0) }
        verify { mockValidator.isValid("two", 1) }
        verify { mockValidator.isValid("three", 2) }
    }

    class ElementValidator
    {
        fun isValid(text: String, index: Int): Boolean
        {
            return text == index.toString()
        }
    }

    @Test
    fun `Should stop as soon as an element is found to be invalid`()
    {
        val validator = spyk<ElementValidator>()

        val list = listOf("foo", "bar", "baz")

        val result = list.allIndexed { index, value -> validator.isValid(value, index) }

        result shouldBe false
        verify { validator.isValid("foo", 0) }
        verifyNotCalled { validator.isValid("bar", 1) }
        verifyNotCalled { validator.isValid("baz", 2) }
    }

    @Test
    fun `Should return the correct result based on whether the contents were all valid`()
    {
        val validator = ElementValidator()

        val invalidList = listOf("0", "1", "2", "4")
        val validList = listOf("0", "1", "2", "3")

        invalidList.allIndexed { index, value -> validator.isValid(value, index) } shouldBe false
        validList.allIndexed { index, value -> validator.isValid(value, index) } shouldBe true
    }

    @Test
    fun `Should sort in the correct order`()
    {
        val list = listOf(1, 3, 2, 5, 4)

        val ascending = list.sortedBy(false) { it }
        val descending = list.sortedBy(true) { it }

        ascending.shouldContainExactly(1, 2, 3, 4, 5)
        descending.shouldContainExactly(5, 4, 3, 2, 1)
    }
}