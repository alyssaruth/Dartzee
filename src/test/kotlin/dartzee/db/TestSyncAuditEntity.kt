package dartzee.db

import dartzee.helper.makeInMemoryDatabaseWithSchema
import org.junit.Test

class TestSyncAuditEntity: AbstractEntityTest<SyncAuditEntity>()
{
    override fun factoryDao() = SyncAuditEntity()

    @Test
    fun `Should report a null lastSyncDate if not recorded before`()
    {
        val db = makeInMemoryDatabaseWithSchema()

    }
}