package dartzee.core.obj

import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestHashMapCount : AbstractTest() {
    @Test
    fun `Should return a total count of 0 by default`() {
        val hm = HashMapCount<Int>()
        hm.getTotalCount() shouldBe 0
    }

    @Test
    fun `Should sum the total count across all keys`() {
        val hm = HashMapCount<Int>()

        hm.incrementCount(5, 100)
        hm.incrementCount(10, 30)
        hm.incrementCount(11, 55)

        hm.getTotalCount() shouldBe 185
    }

    @Test
    fun `Should return the right individual count for a key`() {
        val hm = HashMapCount<Int>()
        hm.getCount(5) shouldBe 0

        hm.incrementCount(5, 5)
        hm.incrementCount(5)

        hm.getCount(5) shouldBe 6
    }

    @Test
    fun `Should return a flattened ordered list`() {
        val hm = HashMapCount<Int>()

        hm.incrementCount(7, 2)
        hm.incrementCount(3, 2)
        hm.incrementCount(5, 1)

        val list = hm.getFlattenedOrderedList(Comparator.comparingInt { it })
        list.shouldContainExactly(3, 3, 5, 7, 7)

        val reverseList = hm.getFlattenedOrderedList(Comparator.comparingInt { -it })
        reverseList.shouldContainExactly(7, 7, 5, 3, 3)
    }
}
