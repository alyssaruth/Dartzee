package dartzee.utils

import dartzee.db.TableName
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.getTableNames
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_NEW_CONNECTION
import dartzee.logging.CODE_SQL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.Severity
import dartzee.logging.exceptions.WrappedSqlException
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TestDatabase: AbstractTest()
{
    @BeforeEach
    fun beforeEach()
    {
        mainDatabase.dropUnexpectedTables()
    }

    @Test
    fun `Should create a new connection if the pool is depleted`()
    {
        usingInMemoryDatabase { db ->
            db.initialiseConnectionPool(1)
            clearLogs()

            //Should borrow from the pool when non-empty
            val conn = db.borrowConnection()
            verifyNoLogs(CODE_NEW_CONNECTION)

            //Should create a new one now that there are none left
            val conn2 = db.borrowConnection()
            verifyLog(CODE_NEW_CONNECTION)

            db.returnConnection(conn2)
            db.returnConnection(conn)

            //Should have returned the connection successfully
            clearLogs()
            db.borrowConnection()
            verifyNoLogs(CODE_NEW_CONNECTION)
        }

    }

    @Test
    fun `Should execute all updates and log them to the console`()
    {
        clearLogs()

        val updates = listOf("CREATE TABLE zzUpdateTest(str VARCHAR(50))", "INSERT INTO zzUpdateTest VALUES ('5')")
        mainDatabase.executeUpdates(updates) shouldBe true

        val records = getLogRecords().filter { it.loggingCode == CODE_SQL }
        records.size shouldBe 2
        records.first().message shouldContain "CREATE TABLE zzUpdateTest(str VARCHAR(50))"
        records.last().message shouldContain "INSERT INTO zzUpdateTest VALUES ('5')"

        mainDatabase.executeQueryAggregate("SELECT COUNT(1) FROM zzUpdateTest") shouldBe 1
    }

    @Test
    fun `Should abort if any updates fail`()
    {
        val updates = listOf("bollocks", "CREATE TABLE zzUpdateTest(str VARCHAR(50))")

        mainDatabase.executeUpdates(updates) shouldBe false
        verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)

        mainDatabase.createTableIfNotExists("zzUpdateTest", "str VARCHAR(50)") shouldBe true
    }

    @Test
    fun `Should log SQLExceptions for failed updates`()
    {
        val update = "CREATE TABLE zzUpdateTest(str INVALID(50))"
        mainDatabase.executeUpdate(update) shouldBe false

        val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        log.message.shouldContain("Caught SQLException for statement: $update")
        log.errorObject!!.message.shouldContain("Syntax error: Encountered \"(\"")
    }

    @Test
    fun `Should execute queries and log them to the console`()
    {
        val updates = listOf("CREATE TABLE zzQueryTest(str VARCHAR(50))",
            "INSERT INTO zzQueryTest VALUES ('RowOne')",
            "INSERT INTO zzQueryTest VALUES ('RowTwo')")

        mainDatabase.executeUpdates(updates)

        val retrievedValues = mutableListOf<String>()
        mainDatabase.executeQuery("SELECT * FROM zzQueryTest").use { rs ->
            while (rs.next())
            {
                retrievedValues.add(rs.getString(1))
            }
        }

        val log = getLastLog()
        log.loggingCode shouldBe CODE_SQL
        log.message shouldContain "SELECT * FROM zzQueryTest"

        retrievedValues.shouldContainExactly("RowOne", "RowTwo")
    }

    @Test
    fun `Should throw an error for failed queries`()
    {
        val query = "SELECT * FROM zzQueryTest"

        val ex = shouldThrow<WrappedSqlException> {
            mainDatabase.executeQuery(query)
        }

        ex.sqlStatement shouldBe query
        ex.sqlException.message shouldContain "does not exist"
    }

    @Test
    fun `Should be possible to connect to separate databases concurrently`()
    {
        usingInMemoryDatabase { dbOne ->
            usingInMemoryDatabase { dbTwo ->
                shouldNotThrowAny {
                    dbOne.initialiseConnectionPool(1)
                    dbTwo.initialiseConnectionPool(1)
                }
            }
        }
    }

    @Test
    fun `Should return null version if version has never been set`()
    {
        mainDatabase.getDatabaseVersion() shouldBe null
    }

    @Test
    fun `Should return the right existing version and support updating it`()
    {
        mainDatabase.updateDatabaseVersion(5)

        mainDatabase.getDatabaseVersion() shouldBe 5
        mainDatabase.updateDatabaseVersion(7)
        mainDatabase.getDatabaseVersion() shouldBe 7

        getCountFromTable("Version", mainDatabase) shouldBe 1
    }

    @Test
    fun `Should support generating local IDs`()
    {
        mainDatabase.generateLocalId(TableName.Game) shouldBe 1
        mainDatabase.generateLocalId(TableName.Game) shouldBe 2
        mainDatabase.generateLocalId(TableName.DartsMatch) shouldBe 1
    }

    @Test
    fun `Should not drop any schema tables`()
    {
        mainDatabase.dropUnexpectedTables().shouldBeEmpty()

        val expectedTableNames = DartsDatabaseUtil.getAllEntitiesIncludingVersion().map { it.getTableNameUpperCase() }
        val tableNames = mainDatabase.getTableNames()
        tableNames.shouldContainExactlyInAnyOrder(expectedTableNames)
    }

    @Test
    fun `Should drop unexpected tables`()
    {
        mainDatabase.createTableIfNotExists("SomeTable", "RowId INT")
        val tmpName = mainDatabase.createTempTable("TempTable", "RowId INT")

        mainDatabase.dropUnexpectedTables().shouldContainExactlyInAnyOrder("SOMETABLE", tmpName?.uppercase())
    }
}