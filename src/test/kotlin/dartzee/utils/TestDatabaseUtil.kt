package dartzee.utils

import dartzee.core.helper.getLogs
import dartzee.core.util.Debug
import dartzee.helper.AbstractTest
import dartzee.helper.dropUnexpectedTables
import dartzee.logging.CODE_SQL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.Severity
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test

class TestDatabaseUtil: AbstractTest()
{
    override fun afterEachTest()
    {
        super.afterEachTest()

        dropUnexpectedTables()
    }

    @Test
    fun `Should create a new connection if the pool is depleted`()
    {
        DatabaseUtil.initialiseConnectionPool(1)
        Debug.waitUntilLoggingFinished()
        Debug.clearLogs()

        //Should borrow from the pool when non-empty
        val conn = DatabaseUtil.borrowConnection()
        getLogs().shouldBeEmpty()

        //Should create a new one now that there are none left
        val conn2 = DatabaseUtil.borrowConnection()
        getLogs().shouldContain("CREATED new connection")

        DatabaseUtil.returnConnection(conn2)
        DatabaseUtil.returnConnection(conn)

        //Should have returned the connection successfully
        Debug.clearLogs()
        DatabaseUtil.borrowConnection()
        getLogs().shouldBeEmpty()
    }

    @Test
    fun `Should execute all updates and log them to the console`()
    {
        clearLogs()

        val updates = listOf("CREATE TABLE zzUpdateTest(str VARCHAR(50))", "INSERT INTO zzUpdateTest VALUES ('5')")
        DatabaseUtil.executeUpdates(updates) shouldBe true

        val records = getLogRecords().filter { it.loggingCode == CODE_SQL }
        records.size shouldBe 2
        records.first().message shouldContain "CREATE TABLE zzUpdateTest(str VARCHAR(50))"
        records.last().message shouldContain "INSERT INTO zzUpdateTest VALUES ('5')"

        DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM zzUpdateTest") shouldBe 1

        DatabaseUtil.dropTable("zzUpdateTest")
    }

    @Test
    fun `Should abort if any updates fail`()
    {
        val updates = listOf("bollucks", "CREATE TABLE zzUpdateTest(str VARCHAR(50))")

        DatabaseUtil.executeUpdates(updates) shouldBe false
        verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)

        DatabaseUtil.createTableIfNotExists("zzUpdateTest", "str VARCHAR(50)") shouldBe true
    }

    @Test
    fun `Should log SQLExceptions for failed updates`()
    {
        val update = "CREATE TABLE zzUpdateTest(str INVALID(50))"
        DatabaseUtil.executeUpdate(update) shouldBe false

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

        DatabaseUtil.executeUpdates(updates)

        val retrievedValues = mutableListOf<String>()
        DatabaseUtil.executeQuery("SELECT * FROM zzQueryTest").use { rs ->
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
    fun `Should log SQLExceptions (and show an error) for failed queries`()
    {
        val query = "SELECT * FROM zzQueryTest"
        DatabaseUtil.executeQuery(query)

        val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        log.message shouldBe "Caught SQLException for statement: $query"
        log.errorObject?.message shouldContain "Table/View 'ZZQUERYTEST' does not exist."
    }
}