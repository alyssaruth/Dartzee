package dartzee.utils

import dartzee.db.DatabaseMigrator
import dartzee.db.MigrationResult
import dartzee.helper.*
import dartzee.logging.CODE_BACKUP_ERROR
import dartzee.logging.CODE_DATABASE_CREATED
import dartzee.logging.CODE_DATABASE_CREATING
import dartzee.logging.Severity
import dartzee.utils.DartsDatabaseUtil.DATABASE_NAME
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.io.File

const val BACKUP_LOCATION = "Test/Backup/Databases"

class TestDartsDatabaseUtil: AbstractTest()
{
    override fun beforeEachTest()
    {
        super.beforeEachTest()
        File(TEST_DB_DIRECTORY).deleteRecursively()
        File(BACKUP_LOCATION).deleteRecursively()

        File(BACKUP_LOCATION).mkdirs()
        File(TEST_DB_DIRECTORY).mkdirs()
        File("$TEST_DB_DIRECTORY/$DATABASE_NAME").mkdirs()
    }

    override fun afterEachTest()
    {
        super.afterEachTest()
        File(TEST_DB_DIRECTORY).deleteRecursively()
        File(BACKUP_LOCATION).deleteRecursively()
    }

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

    @Test
    fun `Should update sync summary`()
    {
        usingInMemoryDatabase { db ->
            shouldUpdateSyncSummary {
                DartsDatabaseUtil.initialiseDatabase(db)
            }
        }
    }

    @Test
    fun `Should not back up any files if file selection cancelled`()
    {
        dialogFactory.directoryToSelect = null

        DartsDatabaseUtil.backupCurrentDatabase()

        dialogFactory.infosShown.shouldBeEmpty()
        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should show an error if an error is thrown when copying the files`()
    {
        File(TEST_DB_DIRECTORY).deleteRecursively()
        dialogFactory.directoryToSelect = File(TEST_DB_DIRECTORY)

        DartsDatabaseUtil.backupCurrentDatabase()

        dialogFactory.errorsShown.shouldContainExactly("There was a problem creating the backup.")
        verifyLog(CODE_BACKUP_ERROR, Severity.ERROR)
    }

    @Test
    fun `Should successfully back up files to the chosen destination`()
    {
        val file = File("$TEST_DB_DIRECTORY/$DATABASE_NAME/File.txt")
        file.createNewFile()

        val selectedDir = File(BACKUP_LOCATION)
        dialogFactory.directoryToSelect = selectedDir

        DartsDatabaseUtil.backupCurrentDatabase()

        dialogFactory.infosShown.shouldContainExactly("Database successfully backed up to ${File("${selectedDir.absolutePath}/$DATABASE_NAME")}")
        dialogFactory.errorsShown.shouldBeEmpty()

        File("${selectedDir.absolutePath}/$DATABASE_NAME/File.txt").shouldExist()
    }
}