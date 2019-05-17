package burlton.core.test.obj

import burlton.core.code.obj.HashMapList
import burlton.core.test.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test

class TestHashMapList: AbstractTest()
{
    @Test
    fun `Should create a new list for a new key`()
    {
        val hm = HashMapList<Int, String>()

        hm.putInList(1, "foo")

        hm[1] shouldBe mutableListOf("foo")
    }

    @Test
    fun `Should add to existing list for an existing key`()
    {
        val hm = HashMapList<Int, String>()

        hm.putInList(1, "foo")
        hm.putInList(1, "bar")

        hm[1] shouldBe mutableListOf("foo", "bar")
    }

    @Test
    fun `Should return all values across differeny keys`()
    {
        val hm = HashMapList<Int, String>()

        hm.putInList(1, "foo")
        hm.putInList(1, "bar")
        hm.putInList(2, "baz")
        hm.putInList(0, "muppet")

        hm[1] shouldBe mutableListOf("foo", "bar")
        hm[2] shouldBe mutableListOf("baz")

        hm.getAllValues().shouldContainExactlyInAnyOrder("foo", "bar", "baz", "muppet")
        hm.getFlattenedValuesSortedByKey().shouldContainExactly("muppet", "foo", "bar", "baz")
    }
}