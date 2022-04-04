package dartzee.screen.stats.overall

import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestLeaderboardUtils: AbstractTest()
{
    @Test
    fun `Should add rankings correctly`()
    {
        val entries = listOf(50, 49, 49, 30, 30, 30, 25, 24).map { LeaderboardEntry(it, emptyList()) }
        val results = getRankedRowsForTable(entries)

        results.map { it[0] } shouldBe listOf (1, 2, 2, 4, 4, 4, 7, 8)
    }

    @Test
    fun `Should preserve the rest of the row entries`()
    {
        val entryOne = LeaderboardEntry(50, listOf("Alice", "#55"))
        val entryTwo = LeaderboardEntry(34, listOf("Bob", "#27"))

        val results = getRankedRowsForTable(listOf(entryOne, entryTwo))

        results.map { it.toList() }.shouldContainExactly(listOf(1, "Alice", "#55"), listOf(2, "Bob", "#27"))
    }
}