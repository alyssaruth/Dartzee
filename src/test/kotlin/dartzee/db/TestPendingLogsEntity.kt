package dartzee.db

import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestPendingLogsEntity: AbstractEntityTest<PendingLogsEntity>()
{
    override fun factoryDao() = PendingLogsEntity()

    @Test
    fun `Should factory with an assigned rowId and the specified JSON`()
    {
        val logJson = "foo"

        val e = PendingLogsEntity.factory(logJson)
        e.logJson shouldBe logJson
        e.rowId.shouldNotBeEmpty()
    }
}
