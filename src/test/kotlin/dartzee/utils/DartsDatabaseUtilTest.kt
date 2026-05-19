package dartzee.utils

import com.github.alyssaburlton.swingtest.clickCancel
import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickYes
import dartzee.db.DatabaseMigrator
import dartzee.db.EntityName
import dartzee.db.MigrationResult
import dartzee.expectErrorDialog
import dartzee.expectInfoDialog
import dartzee.findErrorDialog
import dartzee.findInfoDialog
import dartzee.getDialogMessage
import dartzee.getFileChooser
import dartzee.getQuestionDialog
import dartzee.helper.AbstractTest
import dartzee.helper.TEST_DB_DIRECTORY
import dartzee.helper.TEST_ROOT
import dartzee.helper.assertExits
import dartzee.helper.insertGame
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_BACKUP_ERROR
import dartzee.logging.CODE_DATABASE_CREATED
import dartzee.logging.CODE_DATABASE_CREATING
import dartzee.logging.CODE_RESTORE_ERROR
import dartzee.logging.CODE_TEST_CONNECTION_ERROR
import dartzee.logging.Severity
import dartzee.runAsync
import dartzee.screen.ScreenCache
import dartzee.screen.game.DartsGameScreen
import dartzee.selectFile
import dartzee.utils.DartsDatabaseUtil.DATABASE_NAME
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.File
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

const val BACKUP_LOCATION = "Test/Backup/Databases"

class DartsDatabaseUtilTest : AbstractTest() {
    @BeforeEach
    fun beforeEach() {
        File(TEST_DB_DIRECTORY).deleteRecursively()
        File(BACKUP_LOCATION).deleteRecursively()

        File(BACKUP_LOCATION).mkdirs()
        File(TEST_DB_DIRECTORY).mkdirs()
        File("$TEST_DB_DIRECTORY/$DATABASE_NAME").mkdirs()
    }

    @AfterEach
    fun afterEach() {
        File(TEST_ROOT).deleteRecursively()
    }

    @Test
    fun `Should exit if DB version is too old`() {
        val migrator = mockk<DatabaseMigrator>(relaxed = true)
        every { migrator.migrateToLatest(any(), any()) } returns MigrationResult.TOO_OLD

        assertExits(1) { DartsDatabaseUtil.migrateDatabase(migrator, mainDatabase) }
    }

    @Test
    fun `Should initialise a fresh database if no version is found`() {
        clearLogs()

        usingInMemoryDatabase { db ->
            DartsDatabaseUtil.initialiseDatabase(db)

            verifyLog(CODE_DATABASE_CREATING)
            verifyLog(CODE_DATABASE_CREATED)

            db.getDatabaseVersion() shouldBe DATABASE_VERSION
        }
    }

    @Test
    fun `Should not back up any files if file selection cancelled`() {
        runAsync { DartsDatabaseUtil.backupCurrentDatabase() }

        getFileChooser("Select").clickCancel(async = true)

        findInfoDialog().shouldBeNull()
        findErrorDialog().shouldBeNull()
    }

    @Test
    fun `Should show an error if an error is thrown when copying the files`() {
        File(TEST_DB_DIRECTORY).deleteRecursively()

        runAsync { DartsDatabaseUtil.backupCurrentDatabase() }
        selectFile(TEST_DB_DIRECTORY, "Select")

        expectErrorDialog("There was a problem creating the backup.")
        verifyLog(CODE_BACKUP_ERROR, Severity.ERROR)
    }

    @Test
    fun `Should successfully back up files to the chosen destination`() {
        val file = File("$TEST_DB_DIRECTORY/$DATABASE_NAME/File.txt")
        file.createNewFile()

        val selectedDir = File(BACKUP_LOCATION)

        runAsync { DartsDatabaseUtil.backupCurrentDatabase() }

        selectFile(selectedDir.absolutePath, "Select")
        expectInfoDialog(
            "Database successfully backed up to ${File("${selectedDir.absolutePath}/$DATABASE_NAME")}"
        )

        File("${selectedDir.absolutePath}/$DATABASE_NAME/File.txt").shouldExist()
    }

    @Test
    fun `Should abort the restore if there are open games`() {
        ScreenCache.addDartsGameScreen("foo", mockk<DartsGameScreen>())

        runAsync { DartsDatabaseUtil.restoreDatabase() }

        expectErrorDialog("You must close all open games before continuing.")
    }

    @Test
    fun `Should not do a restore if file selection is cancelled`() {
        runAsync { DartsDatabaseUtil.restoreDatabase() }

        expectInfoDialog("Select the 'Darts' folder you want to restore from.")

        val chooserDialog = getFileChooser("Select")
        chooserDialog.clickCancel(async = true)

        findInfoDialog { it.isVisible }.shouldBeNull()
        findErrorDialog().shouldBeNull()
    }

    @Test
    fun `Should show an error and not do the restore if the selected folder has the wrong name`() {
        runAsync { DartsDatabaseUtil.restoreDatabase() }
        expectInfoDialog("Select the 'Darts' folder you want to restore from.")

        selectFile(File(BACKUP_LOCATION).absolutePath, "Select")

        expectErrorDialog(
            "Selected path is not valid - you must select a folder named '$DATABASE_NAME'"
        )
    }

    @Test
    fun `Should show an error and tidy up if an exception is thrown during the copy`() {
        Database(DartsDatabaseUtil.OTHER_DATABASE_NAME).getDirectory().mkdirs()

        val path = File("$BACKUP_LOCATION/Darts").absolutePath

        runAsync { DartsDatabaseUtil.restoreDatabase() }
        expectInfoDialog("Select the 'Darts' folder you want to restore from.")

        selectFile(path, "Select")

        expectErrorDialog("There was a problem restoring the database.")

        verifyLog(CODE_RESTORE_ERROR, Severity.ERROR)
        Database(DartsDatabaseUtil.OTHER_DATABASE_NAME).getDirectory().shouldNotExist()
    }

    @Test
    fun `Should abort the restore if validation of the selected database fails`() {
        val backupLocation = File("$BACKUP_LOCATION/Darts")
        backupLocation.mkdirs()

        runAsync { DartsDatabaseUtil.restoreDatabase() }
        expectInfoDialog("Select the 'Darts' folder you want to restore from.")

        selectFile(backupLocation.absolutePath, "Select")

        expectErrorDialog("An error occurred connecting to the selected database.")
        verifyLog(CODE_TEST_CONNECTION_ERROR, Severity.ERROR)
    }

    @Test
    fun `Should abort the restore if cancelled after validation succeeds`() {
        usingInMemoryDatabase(withSchema = true) { db ->
            runAsync { DartsDatabaseUtil.validateAndRestoreDatabase(db) }

            val question = getQuestionDialog()
            question.getDialogMessage() shouldBe
                "Successfully connected to target database.\n\nAre you sure you want to restore this database? All current data will be lost."
            question.clickNo(async = true)

            // Main DB connection should be intact
            shouldNotThrowAny { mainDatabase.borrowConnection() }
        }
    }

    @Test
    fun `Should swap in the selected database if confirmed, and clear localId cache`() {
        usingInMemoryDatabase(withSchema = true) { db ->
            mainDatabase.generateLocalId(EntityName.Game) shouldBe 1L
            insertGame(localId = 5L)

            val f = File("${db.getDirectoryStr()}/SomeFile.txt")
            f.createNewFile()

            runAsync { DartsDatabaseUtil.validateAndRestoreDatabase(db) }

            val question = getQuestionDialog()
            question.getDialogMessage() shouldBe
                "Successfully connected to target database.\n\nAre you sure you want to restore this database? All current data will be lost."
            question.clickYes(async = true)

            expectInfoDialog("Database restored successfully.")

            File("${InjectedThings.databaseDirectory}/Darts/SomeFile.txt").shouldExist()
            mainDatabase.generateLocalId(EntityName.Game) shouldBe 6L
        }
    }

    @Test
    fun `Should correctly list all entities`() {
        val entityNameStatics = EntityName.entries.toList()
        val entityNames =
            DartsDatabaseUtil.getAllEntitiesIncludingVersion().map { it.getTableName() }

        entityNames.shouldContainExactlyInAnyOrder(entityNameStatics)
    }
}
