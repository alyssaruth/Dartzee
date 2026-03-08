package dartzee.utils

import com.github.alyssaburlton.swingtest.clickNo
import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.clickYes
import com.github.alyssaburlton.swingtest.findWindow
import dartzee.db.DatabaseMigrator
import dartzee.db.EntityName
import dartzee.db.MigrationResult
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.getInfoDialog
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
import dartzee.utils.DartsDatabaseUtil.DATABASE_NAME
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotExist
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.File
import javax.swing.JDialog
import javax.swing.JOptionPane
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

const val BACKUP_LOCATION = "Test/Backup/Databases"

class TestDartsDatabaseUtil : AbstractTest() {
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
        dialogFactory.directoryToSelect = null

        DartsDatabaseUtil.backupCurrentDatabase()

        findWindow<JDialog>() shouldBe null
    }

    @Test
    fun `Should show an error if an error is thrown when copying the files`() {
        File(TEST_DB_DIRECTORY).deleteRecursively()
        dialogFactory.directoryToSelect = File(TEST_DB_DIRECTORY)

        runAsync { DartsDatabaseUtil.backupCurrentDatabase() }

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "There was a problem creating the backup."
        dlg.clickOk()

        verifyLog(CODE_BACKUP_ERROR, Severity.ERROR)
    }

    @Test
    fun `Should successfully back up files to the chosen destination`() {
        val file = File("$TEST_DB_DIRECTORY/$DATABASE_NAME/File.txt")
        file.createNewFile()

        val selectedDir = File(BACKUP_LOCATION)
        dialogFactory.directoryToSelect = selectedDir

        runAsync { DartsDatabaseUtil.backupCurrentDatabase() }

        val dlg = getInfoDialog()
        dlg.getDialogMessage() shouldBe
            "Database successfully backed up to ${File("${selectedDir.absolutePath}/$DATABASE_NAME")}"
        dlg.clickOk()

        File("${selectedDir.absolutePath}/$DATABASE_NAME/File.txt").shouldExist()
    }

    @Test
    fun `Should abort the restore if there are open games`() {
        ScreenCache.addDartsGameScreen("foo", mockk<DartsGameScreen>())

        runAsync { DartsDatabaseUtil.restoreDatabase() }

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "You must close all open games before continuing."
    }

    @Test
    fun `Should not do a restore if file selection is cancelled`() {
        dialogFactory.directoryToSelect = null

        runAsync { DartsDatabaseUtil.restoreDatabase() }
        val info = getInfoDialog()
        info.clickOk(async = true)
    }

    @Test
    fun `Should show an error and not do the restore if the selected folder has the wrong name`() {
        dialogFactory.directoryToSelect = File(BACKUP_LOCATION)

        runAsync { DartsDatabaseUtil.restoreDatabase() }
        getInfoDialog().clickOk(async = true)

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe
            "Selected path is not valid - you must select a folder named '$DATABASE_NAME'"
    }

    @Test
    fun `Should show an error and tidy up if an exception is thrown during the copy`() {
        Database(DartsDatabaseUtil.OTHER_DATABASE_NAME).getDirectory().mkdirs()

        dialogFactory.directoryToSelect = File("$BACKUP_LOCATION/Darts")

        runAsync { DartsDatabaseUtil.restoreDatabase() }
        getInfoDialog().clickOk(async = true)

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "There was a problem restoring the database."
        dlg.clickOk(async = true)

        verifyLog(CODE_RESTORE_ERROR, Severity.ERROR)
        Database(DartsDatabaseUtil.OTHER_DATABASE_NAME).getDirectory().shouldNotExist()
    }

    @Test
    fun `Should abort the restore if validation of the selected database fails`() {
        val backupLocation = File("$BACKUP_LOCATION/Darts")
        backupLocation.mkdirs()

        dialogFactory.directoryToSelect = backupLocation

        runAsync { DartsDatabaseUtil.restoreDatabase() }
        getInfoDialog().clickOk(async = true)

        val dlg = getErrorDialog()
        dlg.getDialogMessage() shouldBe "An error occurred connecting to the selected database."
        dlg.clickOk(async = true)

        verifyLog(CODE_TEST_CONNECTION_ERROR, Severity.ERROR)
    }

    @Test
    fun `Should abort the restore if cancelled after validation succeeds`() {
        usingInMemoryDatabase(withSchema = true) { db ->
            dialogFactory.questionOption = JOptionPane.NO_OPTION
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

            val info = getInfoDialog()
            info.getDialogMessage() shouldBe "Database restored successfully."
            info.clickOk(async = true)

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
