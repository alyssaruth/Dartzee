package dartzee.utils

import dartzee.db.DatabaseMigrator
import dartzee.db.MigrationResult
import dartzee.helper.AbstractTest
import dartzee.helper.assertExits
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_DATABASE_CREATED
import dartzee.logging.CODE_DATABASE_CREATING
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class TestDartsDatabaseUtil: AbstractTest()
{
    @Test
    fun `Should exit if DB version is too old`()
    {
        val migrator = mockk<DatabaseMigrator>(relaxed = true)
        every { migrator.migrateToLatest(any(), any()) } returns MigrationResult.TOO_OLD

        assertExits(1) {
            DartsDatabaseUtil.migrateDatabase(migrator, mainDatabase)
        }
    }

    @Test
    fun `Should initialise a fresh database if no version is found`()
    {
        clearLogs()

        usingInMemoryDatabase { db ->
            DartsDatabaseUtil.initialiseDatabase(db)

            verifyLog(CODE_DATABASE_CREATING)
            verifyLog(CODE_DATABASE_CREATED)

            db.getDatabaseVersion() shouldBe DATABASE_VERSION
        }
    }
}