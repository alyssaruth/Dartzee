package dartzee.stats

import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestThreeDartScoreWrapper : AbstractTest() {
    @Test
    fun `Should report the correct total across all methods`() {
        val wrapper = ThreeDartScoreWrapper()
        wrapper.getTotalCount() shouldBe 0

        wrapper.addDartStr("20, 5, 1", 100)
        wrapper.addDartStr("20, 5, 1", 101)
        wrapper.addDartStr("20, 3, 3", 102)

        wrapper.getTotalCount() shouldBe 3
    }

    @Test
    fun `Should return one row per method, with the first example game ID`() {
        val wrapper = ThreeDartScoreWrapper()

        wrapper.addDartStr("20, 5, 1", 100)
        wrapper.addDartStr("20, 5, 1", 101)
        wrapper.addDartStr("20, 3, 3", 102)

        val rows = wrapper.createRows()
        rows.size shouldBe 2
        rows
            .map { it.toList() }
            .shouldContainExactlyInAnyOrder(
                listOf("20, 5, 1", 2, 100L),
                listOf("20, 3, 3", 1, 102L),
            )
    }
}
