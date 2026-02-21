package dartzee.db

import com.github.alyssaburlton.swingtest.clickOk
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class TestForeignDatabaseValidator : AbstractTest() {
    @Test
    fun `Should return false if connecting to remote database fails`() {
        val remote = mockk<Database>()
        every { remote.testConnection() } returns false

        val validator = makeValidator()
        var result = true
        runAsync { result = validator.validateAndMigrateForeignDatabase(remote, "remote") }

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "An error occurred connecting to the remote database."
        dlg.clickOk(async = true)

        result shouldBe false
    }

    @Test
    fun `Should return false and log an error if remote database version cannot be verified`() {
        val remoteDatabase = mockk<Database>(relaxed = true)
        every { remoteDatabase.testConnection() } returns true
        every { remoteDatabase.getDatabaseVersion() } returns null

        val validator = makeValidator()
        var result = true
        runAsync {
            result = validator.validateAndMigrateForeignDatabase(remoteDatabase, "selected")
        }

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "An error occurred connecting to the selected database."
        dlg.clickOk(async = true)

        val log = verifyLog(CODE_MERGE_ERROR, Severity.ERROR)
        log.message shouldBe
            "Unable to ascertain selected database version (but could connect) - this is unexpected."
        result shouldBe false
    }

    @Test
    fun `Should return false if remote database has higher version`() {
        usingInMemoryDatabase { remoteDatabase ->
            remoteDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION + 1)

            val validator = makeValidator()
            var result = true
            runAsync {
                result = validator.validateAndMigrateForeignDatabase(remoteDatabase, "other")
            }

            val dlg = getErrorDialog()
            dlg.getDialogMessage() shouldBe
                "The other database contains data written by a higher Dartzee version. \n\nYou will need to update to the latest version of Dartzee before continuing."
            dlg.clickOk(async = true)

            result shouldBe false
        }
    }

    @Test
    fun `Should return false if unable to migrate remote database`() {
        usingInMemoryDatabase(withSchema = true) { remoteDatabase ->
            val dbVersion = DartsDatabaseUtil.DATABASE_VERSION - 1
            remoteDatabase.updateDatabaseVersion(dbVersion)

            val migrator = DatabaseMigrator(emptyMap())
            val validator = makeValidator(databaseMigrator = migrator)
            var result = true
            runAsync {
                result = validator.validateAndMigrateForeignDatabase(remoteDatabase, "other")
            }

            val dbDetails =
                "Other version: $dbVersion, min supported: ${DartsDatabaseUtil.DATABASE_VERSION}, current: ${DartsDatabaseUtil.DATABASE_VERSION}"

            val dlg = getErrorDialog()
            dlg.getDialogMessage() shouldBe
                "Other database is too out-of-date to be upgraded by this version of Dartzee. " +
                    "Please downgrade to an earlier version so that the data can be converted.\n\n$dbDetails"

            dlg.clickOk(async = true)
            result shouldBe false
        }
    }

    @Test
    fun `Should migrate remote database to latest version and return true on success`() {
        usingInMemoryDatabase(withSchema = true) { remoteDatabase ->
            val dbVersion = DartsDatabaseUtil.DATABASE_VERSION - 1
            remoteDatabase.updateDatabaseVersion(dbVersion)

            val migrations =
                mapOf(
                    dbVersion to
                        listOf { database: Database ->
                            database.executeUpdate("CREATE TABLE Test(RowId VARCHAR(36))")
                        }
                )

            val migrator = DatabaseMigrator(migrations)
            val validator = makeValidator(migrator)
            val result = validator.validateAndMigrateForeignDatabase(remoteDatabase, "remote")
            result shouldBe true

            remoteDatabase.getDatabaseVersion() shouldBe DartsDatabaseUtil.DATABASE_VERSION
            remoteDatabase.executeQueryAggregate("SELECT COUNT(1) FROM Test") shouldBe 0
        }
    }

    private fun makeValidator(databaseMigrator: DatabaseMigrator = DatabaseMigrator(emptyMap())) =
        ForeignDatabaseValidator(databaseMigrator)
}
