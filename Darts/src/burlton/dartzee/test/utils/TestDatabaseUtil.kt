package burlton.dartzee.test.utils

import burlton.core.code.util.Debug
import burlton.dartzee.code.utils.DatabaseUtil
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test

class TestDatabaseUtil: AbstractTest()
{
    @Test
    fun `Should create a new connection if the pool is depleted`()
    {
        DatabaseUtil.initialiseConnectionPool(1)

        Debug.clearLogs()

        //Should borrow from the pool when non-empty
        val conn = DatabaseUtil.borrowConnection()
        Debug.getLogs().shouldBeEmpty()

        //Should create a new one now that there are none left
        val conn2 = DatabaseUtil.borrowConnection()
        Debug.getLogs().shouldContain("CREATED new connection")

        DatabaseUtil.returnConnection(conn2)
        DatabaseUtil.returnConnection(conn)

        //Should have returned the connection successfully
        Debug.clearLogs()
        DatabaseUtil.borrowConnection()
        Debug.getLogs().shouldBeEmpty()
    }

    @Test
    fun `Should execute all updates and log them to the console`()
    {
        val updates = listOf("CREATE TABLE zzUpdateTest(str VARCHAR(50))", "INSERT INTO zzUpdateTest VALUES ('5')")

        DatabaseUtil.executeUpdates(updates) shouldBe true

        Debug.getLogs().shouldContain("CREATE TABLE zzUpdateTest(str VARCHAR(50));")
        Debug.getLogs().shouldContain("INSERT INTO zzUpdateTest VALUES ('5');")

        DatabaseUtil.executeQueryAggregate("SELECT COUNT(1) FROM zzUpdateTest") shouldBe 1

        DatabaseUtil.dropTable("zzUpdateTest")
    }

    @Test
    fun `Should abort if any updates fail`()
    {
        val updates = listOf("bollucks", "CREATE TABLE zzUpdateTest(str VARCHAR(50))")

        DatabaseUtil.executeUpdates(updates) shouldBe false
        DatabaseUtil.createTableIfNotExists("zzUpdateTest", "str VARCHAR(50)") shouldBe true

        DatabaseUtil.dropTable("zzUpdateTest")
    }

    @Test
    fun `Should log SQLExceptions for failed updates`()
    {
        val update = "CREATE TABLE zzUpdateTest(str INVALID(50))"
        Debug.setLogToSystemOut(true)
        DatabaseUtil.executeUpdate(update) shouldBe false
        Debug.getLogs().shouldContain("Caught SQLException for query: $update")
        Debug.getLogs().shouldContain("Syntax error: Encountered \"(\"")
    }
}