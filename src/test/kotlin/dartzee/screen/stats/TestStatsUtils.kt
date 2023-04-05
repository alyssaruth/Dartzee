package dartzee.screen.stats

import dartzee.helper.AbstractTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestStatsUtils : AbstractTest()
{
    @Test
    fun `Should correctly compute the median`()
    {
        listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).shuffled().median() shouldBe 5.5
        listOf(1, 2, 3, 4, 5, 6, 7, 8, 9).shuffled().median() shouldBe 5.0
        listOf(4).median() shouldBe 4.0
        listOf(1, 1, 1, 5).median() shouldBe 1.0
        emptyList<Int>().median() shouldBe 0.0
    }
}