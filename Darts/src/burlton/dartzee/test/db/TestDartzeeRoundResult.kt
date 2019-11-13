package burlton.dartzee.test.db

import burlton.dartzee.code.db.DartzeeRoundResult
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test

class TestDartzeeRoundResult: AbstractEntityTest<DartzeeRoundResult>()
{
    override fun factoryDao() = DartzeeRoundResult()

    @Test
    fun `Should be indexed on PlayerId_ParticipantId_RoundNumber`()
    {
        val indexes = mutableListOf<List<String>>()
        factoryDao().addListsOfColumnsForIndexes(indexes)

        indexes.first().shouldContainExactly("PlayerId", "ParticipantId", "RoundNumber")
    }
}