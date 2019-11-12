package burlton.dartzee.test.db

import burlton.dartzee.code.db.RoundDetailEntity
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test

class TestRoundDetailEntity: AbstractEntityTest<RoundDetailEntity>()
{
    override fun factoryDao() = RoundDetailEntity()

    @Test
    fun `Should be indexed on PlayerId_ParticipantId_RoundNumber`()
    {
        val indexes = mutableListOf<List<String>>()
        factoryDao().addListsOfColumnsForIndexes(indexes)

        indexes.shouldContainExactly(listOf("PlayerId", "ParticipantId", "RoundNumber"))
    }
}