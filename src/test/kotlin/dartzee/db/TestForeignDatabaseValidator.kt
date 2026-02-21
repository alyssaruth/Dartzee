package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.logging.Severity
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import io.kotest.matchers.collections.shouldContainExactly
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
        validator.validateAndMigrateForeignDatabase(remote, "remote") shouldBe false
        dialogFactory.errorsShown.shouldContainExactly(
            "An error occurred connecting to the remote database."
        )
    }

    @Test
    fun `Should return false and log an error if remote database version cannot be verified`() {
        val remoteDatabase = mockk<Database>(relaxed = true)
        every { remoteDatabase.testConnection() } returns true
        every { remoteDatabase.getDatabaseVersion() } returns null

        val validator = makeValidator()
        validator.validateAndMigrateForeignDatabase(remoteDatabase, "selected") shouldBe false

        val log = verifyLog(CODE_MERGE_ERROR, Severity.ERROR)
        log.message shouldBe
            "Unable to ascertain selected database version (but could connect) - this is unexpected."
        dialogFactory.errorsShown.shouldContainExactly(
            "An error occurred connecting to the selected database."
        )
    }

    @Test
    fun `Should return false if remote database has higher version`() {
        usingInMemoryDatabase { remoteDatabase ->
            remoteDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION + 1)

            val validator = makeValidator()
            validator.validateAndMigrateForeignDatabase(remoteDatabase, "other") shouldBe false
            dialogFactory.errorsShown.shouldContainExactly(
                "The other database contains data written by a higher Dartzee version. \n\nYou will need to update to the latest version of Dartzee before continuing."
            )
        }
    }

    @Test
    fun `Should return false if unable to migrate remote database`() {
        usingInMemoryDatabase(withSchema = true) { remoteDatabase ->
            val dbVersion = DartsDatabaseUtil.DATABASE_VERSION - 1
            remoteDatabase.updateDatabaseVersion(dbVersion)

            val migrator = DatabaseMigrator(emptyMap())
            val validator = makeValidator(databaseMigrator = migrator)
            val result = validator.validateAndMigrateForeignDatabase(remoteDatabase, "other")
            result shouldBe false

            val dbDetails =
                "Other version: $dbVersion, min supported: ${DartsDatabaseUtil.DATABASE_VERSION}, current: ${DartsDatabaseUtil.DATABASE_VERSION}"
            dialogFactory.errorsShown.shouldContainExactly(
                "Other database is too out-of-date to be upgraded by this version of Dartzee. " +
                    "Please downgrade to an earlier version so that the data can be converted.\n\n$dbDetails"
            )
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
