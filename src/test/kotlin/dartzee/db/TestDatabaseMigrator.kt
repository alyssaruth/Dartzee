package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.getTableNames
import dartzee.helper.makeInMemoryDatabase
import dartzee.logging.*
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.Database
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import org.junit.Test

class TestDatabaseMigrator: AbstractTest()
{
    @Test
    fun `Should initialise a fresh database if no version is found`()
    {
        clearLogs()

        val database = makeInMemoryDatabase()
        val migrator = DatabaseMigrator(emptyMap())
        val result = migrator.migrateToLatest(database, "Test")
        result shouldBe MigrationResult.SUCCESS

        verifyLog(CODE_DATABASE_CREATING)
        verifyLog(CODE_DATABASE_CREATED)

        database.getDatabaseVersion() shouldBe DATABASE_VERSION

        val expectedTableNames = DartsDatabaseUtil.getAllEntitiesIncludingVersion().map { it.getTableNameUpperCase() }
        val tableNames = getTableNames(database)
        tableNames.shouldContainExactlyInAnyOrder(expectedTableNames)
    }

    @Test
    fun `Should not carry out a migration if database is already on the latest version`()
    {
        clearLogs()

        val database = makeInMemoryDatabase()
        database.updateDatabaseVersion(DATABASE_VERSION)

        val migrator = DatabaseMigrator(emptyMap())
        val result = migrator.migrateToLatest(database, "Test")
        result shouldBe MigrationResult.SUCCESS

        verifyLog(CODE_DATABASE_UP_TO_DATE)

        getTableNames(database).shouldContainExactly("VERSION")
    }

    @Test
    fun `Should not carry out a migration if database version is too old based on migrations passed in`()
    {
        clearLogs()

        val migrations = mapOf(13 to listOf { _: Database -> true }, 14 to listOf { _: Database -> true })

        val database = makeInMemoryDatabase()
        val oldVersion = 12
        database.updateDatabaseVersion(oldVersion)

        val migrator = DatabaseMigrator(migrations)
        val result = migrator.migrateToLatest(database, "Test")
        result shouldBe MigrationResult.TOO_OLD

        verifyLog(CODE_DATABASE_TOO_OLD, Severity.WARN)
        getTableNames(database).shouldContainExactly("VERSION")

        val dbDetails = "Test version: $oldVersion, min supported: 13, current: $DATABASE_VERSION"

        dialogFactory.errorsShown.shouldContainExactly("Test database is too out-of-date to be upgraded by this version of Dartzee. " +
                "Please downgrade to an earlier version so that the data can be converted.\n\n$dbDetails")
    }

    @Test
    fun `Should run conversions in sequence to get to latest version`()
    {
        val database = makeInMemoryDatabase()
        database.updateDatabaseVersion(DATABASE_VERSION - 2)

        val migration1a = { db: Database -> db.executeUpdate("CREATE TABLE Test(RowId VARCHAR(36) PRIMARY KEY)") }
        val migration1b = { db: Database -> db.executeUpdate("ALTER TABLE Test ADD TestCol2 INT") }
        val migration2 = { db: Database -> db.executeUpdate("RENAME COLUMN Test.TestCol2 TO IntCol2") }

        val map = mapOf(DATABASE_VERSION - 2 to listOf(migration1a, migration1b),
            DATABASE_VERSION - 1 to listOf(migration2))

        val migrator = DatabaseMigrator(map)
        val result = migrator.migrateToLatest(database, "Test")
        result shouldBe MigrationResult.SUCCESS

        // Logging
        val logs = getLogRecords().filter { it.loggingCode == CODE_DATABASE_NEEDS_UPDATE }
        logs.map { it.message }.shouldContainExactly(
            "Upgrading Test database to V${DATABASE_VERSION - 1} (2 migrations)",
            "Upgrading Test database to V${DATABASE_VERSION} (1 migrations)"
        )

        // Should be on latest version and have expected metadata
        database.getDatabaseVersion() shouldBe DATABASE_VERSION
        shouldNotThrowAny {
            database.executeQuery("SELECT RowId, IntCol2 FROM Test")
        }
    }
}