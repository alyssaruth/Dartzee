package dartzee.db

import dartzee.core.util.getSqlDateNow
import dartzee.helper.AbstractTest
import dartzee.helper.dropUnexpectedTables
import dartzee.helper.getCountFromTable
import dartzee.helper.makeInMemoryDatabase
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp
import java.util.*

class TestAbstractEntity: AbstractTest()
{
    var otherDatabase = makeInMemoryDatabase()

    override fun beforeEachTest()
    {
        super.beforeEachTest()
        FakeEntity().createTable()
        otherDatabase = makeInMemoryDatabase()
        FakeEntity(otherDatabase).createTable()
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

    @Test
    fun `Should insert into other database if row does not already exist`()
    {
        val entity = insertFakeEntity(testString = "carrot", dtLastUpdate = Timestamp(500))
        getCountFromTable("TestTable", mainDatabase) shouldBe 1
        getCountFromTable("TestTable", otherDatabase) shouldBe 0

        entity.mergeIntoDatabase(otherDatabase)
        getCountFromTable("TestTable", otherDatabase) shouldBe 1

        val otherEntity = FakeEntity(otherDatabase).retrieveForId(entity.rowId)!!
        otherEntity.testString shouldBe "carrot"
        otherEntity.dtLastUpdate shouldBe Timestamp(500)
    }

    @Test
    fun `Should update other database if dtLastUpdate is more recent`()
    {
        val localEntity = insertFakeEntity(testString = "carrot", dtLastUpdate = Timestamp(500), database = mainDatabase)
        insertFakeEntity(rowId = localEntity.rowId, testString = "banana", dtLastUpdate = Timestamp(499), database = otherDatabase)

        localEntity.mergeIntoDatabase(otherDatabase)
        getCountFromTable("TestTable", otherDatabase) shouldBe 1

        val otherEntity = FakeEntity(otherDatabase).retrieveForId(localEntity.rowId)!!
        otherEntity.testString shouldBe "carrot"
        otherEntity.dtLastUpdate shouldBe Timestamp(500)
    }

    @Test
    fun `Should not update other database if dtLastUpdate is older`()
    {
        val localEntity = insertFakeEntity(testString = "carrot", dtLastUpdate = Timestamp(500), database = mainDatabase)
        insertFakeEntity(rowId = localEntity.rowId, testString = "banana", dtLastUpdate = Timestamp(501), database = otherDatabase)

        localEntity.mergeIntoDatabase(otherDatabase)
        getCountFromTable("TestTable", otherDatabase) shouldBe 1

        val otherEntity = FakeEntity(otherDatabase).retrieveForId(localEntity.rowId)!!
        otherEntity.testString shouldBe "banana"
        otherEntity.dtLastUpdate shouldBe Timestamp(501)
    }

    private fun insertFakeEntity(rowId: String = UUID.randomUUID().toString(), testString: String = "", dtLastUpdate: Timestamp = getSqlDateNow(), database: Database = mainDatabase): FakeEntity
    {
        val entity = FakeEntity(database)
        entity.rowId = rowId
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