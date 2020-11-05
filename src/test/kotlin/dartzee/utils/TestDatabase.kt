package dartzee.utils

import dartzee.helper.*
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
import org.junit.Test

class TestDatabase: AbstractTest()
{
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

        usingInMemoryDatabase { db ->
            val updates = listOf("CREATE TABLE zzUpdateTest(str VARCHAR(50))", "INSERT INTO zzUpdateTest VALUES ('5')")
            db.executeUpdates(updates) shouldBe true

            val records = getLogRecords().filter { it.loggingCode == CODE_SQL }
            records.size shouldBe 2
            records.first().message shouldContain "CREATE TABLE zzUpdateTest(str VARCHAR(50))"
            records.last().message shouldContain "INSERT INTO zzUpdateTest VALUES ('5')"

            db.executeQueryAggregate("SELECT COUNT(1) FROM zzUpdateTest") shouldBe 1
        }
    }

    @Test
    fun `Should abort if any updates fail`()
    {
        usingInMemoryDatabase { db ->
            val updates = listOf("bollucks", "CREATE TABLE zzUpdateTest(str VARCHAR(50))")

            db.executeUpdates(updates) shouldBe false
            verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)

            db.createTableIfNotExists("zzUpdateTest", "str VARCHAR(50)") shouldBe true
        }
    }

    @Test
    fun `Should log SQLExceptions for failed updates`()
    {
        usingInMemoryDatabase { db ->
            val update = "CREATE TABLE zzUpdateTest(str INVALID(50))"
            db.executeUpdate(update) shouldBe false

            val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
            log.message.shouldContain("Caught SQLException for statement: $update")
            log.errorObject!!.message.shouldContain("Syntax error: Encountered \"(\"")
        }
    }

    @Test
    fun `Should execute queries and log them to the console`()
    {
        usingInMemoryDatabase { db ->
            val updates = listOf("CREATE TABLE zzQueryTest(str VARCHAR(50))",
                "INSERT INTO zzQueryTest VALUES ('RowOne')",
                "INSERT INTO zzQueryTest VALUES ('RowTwo')")

            db.executeUpdates(updates)

            val retrievedValues = mutableListOf<String>()
            db.executeQuery("SELECT * FROM zzQueryTest").use { rs ->
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
    }

    @Test
    fun `Should log SQLExceptions (and show an error) for failed queries`()
    {
        usingInMemoryDatabase { db ->
            val query = "SELECT * FROM zzQueryTest"

            val ex = shouldThrow<WrappedSqlException> {
                db.executeQuery(query)
            }

            ex.sqlStatement shouldBe query
            ex.sqlException.message shouldContain "does not exist"
        }
    }

    @Test
    fun `Should be possible to connect to separate databases concurrently`()
    {
        usingInMemoryDatabase { dbOne ->
            usingInMemoryDatabase { dbTwo ->
                shouldNotThrowAny {
                    dbOne.initialiseConnectionPool(5)
                    dbTwo.initialiseConnectionPool(5)
                }
            }
        }
    }

    @Test
    fun `Should return null version if version has never been set`()
    {
        usingInMemoryDatabase { it.getDatabaseVersion() shouldBe null }
    }

    @Test
    fun `Should return the right existing version and support updating it`()
    {
        usingInMemoryDatabase { db ->
            db.updateDatabaseVersion(5)

            db.getDatabaseVersion() shouldBe 5
            db.updateDatabaseVersion(7)
            db.getDatabaseVersion() shouldBe 7

            getCountFromTable("Version", db) shouldBe 1
        }
    }

    @Test
    fun `Should support generating local IDs`()
    {
        usingInMemoryDatabase(withSchema = true) { database ->
            database.generateLocalId("Game") shouldBe 1
            database.generateLocalId("Game") shouldBe 2
            database.generateLocalId("DartsMatch") shouldBe 1
        }
    }

    @Test
    fun `Should not drop any schema tables`()
    {
        usingInMemoryDatabase(withSchema = true) { database ->
            database.dropUnexpectedTables().shouldBeEmpty()

            val expectedTableNames = DartsDatabaseUtil.getAllEntitiesIncludingVersion().map { it.getTableNameUpperCase() }
            val tableNames = database.getTableNames()
            tableNames.shouldContainExactlyInAnyOrder(expectedTableNames)
        }
    }

    @Test
    fun `Should drop unexpected tables`()
    {
        mainDatabase.createTableIfNotExists("SomeTable", "RowId INT")
        val tmpName = mainDatabase.createTempTable("TempTable", "RowId INT")

        mainDatabase.dropUnexpectedTables().shouldContainExactlyInAnyOrder("SOMETABLE", tmpName?.toUpperCase())
    }
}