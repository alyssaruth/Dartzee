package dartzee.db

import com.github.alyssaburlton.swingtest.clickOk
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.getTableNames
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_DATABASE_CREATED
import dartzee.logging.CODE_DATABASE_CREATING
import dartzee.logging.CODE_DATABASE_NEEDS_UPDATE
import dartzee.logging.CODE_DATABASE_TOO_OLD
import dartzee.logging.CODE_DATABASE_UP_TO_DATE
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.Database
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrator : AbstractTest() {
    @Test
    fun `Should initialise a fresh database if no version is found`() {
        clearLogs()

        usingInMemoryDatabase { db ->
            val migrator = DatabaseMigrator(emptyMap())
            val result = migrator.migrateToLatest(db, "Test")
            result shouldBe MigrationResult.SUCCESS

            verifyLog(CODE_DATABASE_CREATING)
            verifyLog(CODE_DATABASE_CREATED)

            db.getDatabaseVersion() shouldBe DATABASE_VERSION

            val expectedTableNames =
                DartsDatabaseUtil.getAllEntitiesIncludingVersion().map {
                    it.getTableNameUpperCase()
                }
            val tableNames = db.getTableNames()
            tableNames.shouldContainExactlyInAnyOrder(expectedTableNames)
        }
    }

    @Test
    fun `Should not carry out a migration if database is already on the latest version`() {
        clearLogs()

        usingInMemoryDatabase { db ->
            db.updateDatabaseVersion(DATABASE_VERSION)

            val migrator = DatabaseMigrator(emptyMap())
            val result = migrator.migrateToLatest(db, "Test")
            result shouldBe MigrationResult.SUCCESS

            verifyLog(CODE_DATABASE_UP_TO_DATE)

            db.getTableNames().shouldContainExactly("VERSION")
        }
    }

    @Test
    fun `Should not carry out a migration if database version is too old based on migrations passed in`() {
        clearLogs()

        val migrations =
            mapOf(13 to listOf { _: Database -> true }, 14 to listOf { _: Database -> true })

        usingInMemoryDatabase { database ->
            val oldVersion = 12
            database.updateDatabaseVersion(oldVersion)

            val migrator = DatabaseMigrator(migrations)
            var result = MigrationResult.SUCCESS
            runAsync { result = migrator.migrateToLatest(database, "Test") }

            val dbDetails =
                "Test version: $oldVersion, min supported: 13, current: $DATABASE_VERSION"

            val errorDialog = getErrorDialog()
            errorDialog.getDialogMessage() shouldBe
                "Test database is too out-of-date to be upgraded by this version of Dartzee. " +
                    "Please downgrade to an earlier version so that the data can be converted.\n\n$dbDetails"
            errorDialog.clickOk(async = true)

            verifyLog(CODE_DATABASE_TOO_OLD, Severity.WARN)
            database.getTableNames().shouldContainExactly("VERSION")
            result shouldBe MigrationResult.TOO_OLD
        }
    }

    @Test
    fun `Should run conversions in sequence to get to latest version`() {
        usingInMemoryDatabase { database ->
            database.updateDatabaseVersion(DATABASE_VERSION - 2)

            val migration1a = { db: Database ->
                db.executeUpdate("CREATE TABLE Test(RowId VARCHAR(36) PRIMARY KEY)")
            }
            val migration1b = { db: Database ->
                db.executeUpdate("ALTER TABLE Test ADD TestCol2 INT")
            }
            val migration2 = { db: Database ->
                db.executeUpdate("RENAME COLUMN Test.TestCol2 TO IntCol2")
            }

            val map =
                mapOf(
                    DATABASE_VERSION - 2 to listOf(migration1a, migration1b),
                    DATABASE_VERSION - 1 to listOf(migration2),
                )

            val migrator = DatabaseMigrator(map)
            val result = migrator.migrateToLatest(database, "Test")
            result shouldBe MigrationResult.SUCCESS

            // Logging
            val logs =
                flushAndGetLogRecords().filter { it.loggingCode == CODE_DATABASE_NEEDS_UPDATE }
            logs
                .map { it.message }
                .shouldContainExactly(
                    "Upgrading Test database to V${DATABASE_VERSION - 1} (2 migrations)",
                    "Upgrading Test database to V${DATABASE_VERSION} (1 migrations)",
                )

            // Should be on latest version and have expected metadata
            database.getDatabaseVersion() shouldBe DATABASE_VERSION
            shouldNotThrowAny { database.executeQuery("SELECT RowId, IntCol2 FROM Test") }
        }
    }
}
