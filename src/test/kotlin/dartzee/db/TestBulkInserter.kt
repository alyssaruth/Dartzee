package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.factoryPlayer
import dartzee.helper.getCountFromTable
import dartzee.helper.insertPlayer
import dartzee.helper.wipeTable
import dartzee.logging.CODE_BULK_SQL
import dartzee.logging.CODE_SQL
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.Severity
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class TestBulkInserter: AbstractTest()
{
    @Test
    fun `Should do nothing if passed no entities to insert`()
    {
        clearLogs()
        BulkInserter.insert()
        flushAndGetLogRecords().shouldBeEmpty()
    }

    @Test
    fun `Should stack trace and do nothing if any entities are retrievedFromDb`()
    {
        val playerOne = PlayerEntity()
        val playerTwo = insertPlayer()

        BulkInserter.insert(playerOne, playerTwo)

        val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        log.message shouldBe "Attempting to bulk insert Player entities, but some are already in the database"
        getCountFromTable("Player") shouldBe 1
    }

    @Test
    fun `Should log SQLExceptions if something goes wrong inserting entities`()
    {
        val playerOne = factoryPlayer("Pete")
        val playerTwo = factoryPlayer("Leah")

        playerOne.rowId = playerTwo.rowId

        BulkInserter.insert(playerOne, playerTwo)

        val log = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        log.errorObject?.message shouldContain "duplicate key value"

        getCountFromTable("Player") shouldBe 0
    }

    @Test
    fun `Should insert the right number of rows per INSERT statement`()
    {
        checkInsertBatching(prepareRows(80), 1, 80)
        checkInsertBatching(prepareRows(80), 20, 4)
        checkInsertBatching(prepareRows(80), 21, 4)
    }
    private fun checkInsertBatching(rows: List<GameEntity>, rowsPerInsert: Int, expectedNumberOfBatches: Int)
    {
        wipeTable(EntityName.Game)
        clearLogs()

        BulkInserter.insert(rows, 1000, rowsPerInsert)

        flushAndGetLogRecords() shouldHaveSize(expectedNumberOfBatches)
        getCountFromTable(EntityName.Game) shouldBe rows.size
    }

    @Test
    fun `Should only run single-threaded successfully for a small number of rows`()
    {
        val rows = prepareRows(50)

        BulkInserter.insert(rows, 50, 5)
        getCountFromTable(EntityName.Game) shouldBe 50
    }

    @Test
    fun `Should run multi-threaded successfully`()
    {
        val rows = prepareRows(50)

        BulkInserter.insert(rows, 5, 5)
        getCountFromTable(EntityName.Game) shouldBe 50
    }

    @Test
    fun `Should temporarily suppress logging for a large number of rows`()
    {
        val rows = prepareRows(501)
        clearLogs()

        BulkInserter.insert(rows, 300, 50)

        flushAndGetLogRecords().filter { it.loggingCode == CODE_SQL }.shouldBeEmpty()
        val log = flushAndGetLogRecords().last { it.loggingCode == CODE_BULK_SQL }
        log.message shouldBe "Inserting 501 rows into Game (2 threads @ 50 rows per insert)"
        getCountFromTable(EntityName.Game) shouldBe 501

        wipeTable(EntityName.Game)
        val moreRows = prepareRows(10)
        BulkInserter.insert(moreRows, 300, 50)

        val newLog = getLastLog()
        newLog.loggingCode shouldBe CODE_SQL
        newLog.message shouldContain "INSERT INTO Game VALUES"
    }

    private fun prepareRows(numberToGenerate: Int) = (1..numberToGenerate).map {
        GameEntity().also {
            it.assignRowId()
        }
    }
}