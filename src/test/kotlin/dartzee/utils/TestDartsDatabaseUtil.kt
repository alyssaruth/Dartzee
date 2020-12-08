package dartzee.utils

import dartzee.db.DatabaseMigrator
import dartzee.db.MigrationResult
import dartzee.helper.*
import dartzee.logging.*
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGameScreen
import dartzee.utils.DartsDatabaseUtil.DATABASE_NAME
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.file.shouldNotExist
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.io.File
import javax.swing.JOptionPane

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
        File(TEST_ROOT).deleteRecursively()
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
        shouldUpdateSyncSummary {
            DartsDatabaseUtil.initialiseDatabase(mainDatabase)
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

    @Test
    fun `Should abort the restore if there are open games`()
    {
        ScreenCache.addDartsGameScreen("foo", mockk<DartsGameScreen>())
        DartsDatabaseUtil.restoreDatabase()
        dialogFactory.errorsShown.shouldContainExactly("You must close all open games before continuing.")
    }

    @Test
    fun `Should not do a restore if file selection is cancelled`()
    {
        dialogFactory.directoryToSelect = null

        DartsDatabaseUtil.restoreDatabase()

        dialogFactory.errorsShown.shouldBeEmpty()
    }

    @Test
    fun `Should show an error and not do the restore if the selected folder has the wrong name`()
    {
        dialogFactory.directoryToSelect = File(BACKUP_LOCATION)

        DartsDatabaseUtil.restoreDatabase()

        dialogFactory.errorsShown.shouldContainExactly("Selected path is not valid - you must select a folder named '$DATABASE_NAME'")
    }

    @Test
    fun `Should show an error and tidy up if an exception is thrown during the copy`()
    {
        Database(DartsDatabaseUtil.OTHER_DATABASE_NAME).getDirectory().mkdirs()

        dialogFactory.directoryToSelect = File("$BACKUP_LOCATION/Darts")

        DartsDatabaseUtil.restoreDatabase()

        dialogFactory.errorsShown.shouldContainExactly("There was a problem restoring the database.")
        verifyLog(CODE_RESTORE_ERROR, Severity.ERROR)
        Database(DartsDatabaseUtil.OTHER_DATABASE_NAME).getDirectory().shouldNotExist()
    }

    @Test
    fun `Should abort the restore if validation of the selected database fails`()
    {
        val backupLocation = File("$BACKUP_LOCATION/Darts")
        backupLocation.mkdirs()

        dialogFactory.directoryToSelect = backupLocation

        DartsDatabaseUtil.restoreDatabase()

        val log = verifyLog(CODE_MERGE_ERROR, Severity.ERROR)
        log.message shouldBe "Unable to ascertain selected database version (but could connect) - this is unexpected."

        dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the selected database.")
    }

    @Test
    fun `Should abort the restore if cancelled after validation succeeds`()
    {
        usingInMemoryDatabase(withSchema = true) { db ->
            dialogFactory.questionOption = JOptionPane.NO_OPTION
            DartsDatabaseUtil.validateAndRestoreDatabase(db)
            dialogFactory.questionsShown.shouldContainExactly("Successfully connected to target database.\n\nAre you sure you want to restore this database? All current data will be lost.")
            dialogFactory.infosShown.shouldBeEmpty()
        }
    }

    @Test
    fun `Should swap in the selected database if confirmed`()
    {
        usingInMemoryDatabase(withSchema = true) { db ->
            dialogFactory.questionOption = JOptionPane.YES_OPTION
            val f = File("${db.getDirectoryStr()}/SomeFile.txt")
            f.createNewFile()

            DartsDatabaseUtil.validateAndRestoreDatabase(db)

            File("${InjectedThings.databaseDirectory}/Darts/SomeFile.txt").shouldExist()

            dialogFactory.questionsShown.shouldContainExactly("Successfully connected to target database.\n\nAre you sure you want to restore this database? All current data will be lost.")
            dialogFactory.infosShown.shouldContainExactly("Database restored successfully.")
        }
    }

}