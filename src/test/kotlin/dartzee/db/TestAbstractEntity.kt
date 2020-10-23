package dartzee.db

import dartzee.core.util.getSqlDateNow
import dartzee.helper.AbstractTest
import dartzee.helper.dropUnexpectedTables
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test
import java.sql.Timestamp

class TestAbstractEntity: AbstractTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()
        FakeEntity().createTable()
    }

    override fun afterEachTest()
    {
        super.afterEachTest()
        dropUnexpectedTables()
    }

    @Test
    fun `Should retrieve all entities if passed a null date`()
    {
        val e1 = insertFakeEntity()
        val e2 = insertFakeEntity()

        val retrieved = FakeEntity().retrieveModifiedSince(null).map { it.rowId }
        retrieved.shouldContainExactly(e1.rowId, e2.rowId)
    }

    @Test
    fun `Should retrieve all entities modified since a particular date`()
    {
        insertFakeEntity(dtLastUpdate = Timestamp(500))
        insertFakeEntity(dtLastUpdate = Timestamp(1000))
        val e3 = insertFakeEntity(dtLastUpdate = Timestamp(1001))
        val e4 = insertFakeEntity(dtLastUpdate = Timestamp(2000))

        val retrieved = FakeEntity().retrieveModifiedSince(Timestamp(1000)).map { it.rowId }
        retrieved.shouldContainExactly(e3.rowId, e4.rowId)
    }

    private fun insertFakeEntity(testString: String = "", dtLastUpdate: Timestamp = getSqlDateNow()): FakeEntity
    {
        val entity = FakeEntity()
        entity.assignRowId()
        entity.testString = testString
        entity.saveToDatabase(dtLastUpdate)
        return entity
    }
}

class FakeEntity(database: Database = mainDatabase): AbstractEntity<FakeEntity>(database)
{
    var testString = ""

    override fun getTableName() = "TestTable"
    override fun getCreateTableSqlSpecific() = "TestString VARCHAR(1000) NOT NULL"
}