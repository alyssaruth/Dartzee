package dartzee.sync

import dartzee.core.util.DialogUtil
import dartzee.core.util.runInOtherThread
import dartzee.db.DatabaseMerger
import dartzee.db.DatabaseMigrator
import dartzee.db.DeletionAuditEntity
import dartzee.db.ForeignDatabaseValidator
import dartzee.db.GameEntity
import dartzee.db.SyncAuditEntity
import dartzee.logging.CODE_PULL_ERROR
import dartzee.logging.CODE_PUSH_ERROR
import dartzee.logging.CODE_REVERT_TO_PULL
import dartzee.logging.CODE_SYNC_ERROR
import dartzee.logging.KEY_GAME_IDS
import dartzee.logging.LoggingCode
import dartzee.logging.exceptions.WrappedSqlException
import dartzee.screen.ScreenCache
import dartzee.screen.sync.SyncManagementScreen
import dartzee.screen.sync.SyncProgressDialog
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.databaseDirectory
import dartzee.utils.InjectedThings.logger
import dartzee.utils.InjectedThings.mainDatabase
import java.io.File
import java.io.InterruptedIOException
import java.net.SocketException
import javax.swing.SwingUtilities

val SYNC_DIR = "${System.getProperty("user.dir")}/Sync"

class SyncManager(private val dbStore: IRemoteDatabaseStore) {
    fun databaseExists(remoteName: String) = dbStore.databaseExists(remoteName)

    fun doPush(remoteName: String) = runInOtherThread { doPushOnOtherThread(remoteName) }

    private fun doPushOnOtherThread(remoteName: String) {
        var auditEntry: SyncAuditEntity? = null

        try {
            SwingUtilities.invokeLater { DialogUtil.showLoadingDialogOLD("Pushing $remoteName...") }
            setUpSyncDir()

            auditEntry = SyncAuditEntity.insertSyncAudit(mainDatabase, remoteName)
            dbStore.pushDatabase(remoteName, mainDatabase)
        } catch (e: Exception) {
            auditEntry?.deleteFromDatabase()
            handleSyncError(e, CODE_PUSH_ERROR)
        } finally {
            tidyUpAllSyncDirs()
            SwingUtilities.invokeLater { DialogUtil.dismissLoadingDialogOLD() }
            ScreenCache.get<SyncManagementScreen>().initialise()
        }
    }

    fun doPull(remoteName: String) = runInOtherThread { doPullOnOtherThread(remoteName) }

    private fun doPullOnOtherThread(remoteName: String) {
        try {
            SwingUtilities.invokeLater { DialogUtil.showLoadingDialogOLD("Pulling $remoteName...") }
            setUpSyncDir()

            val remote = dbStore.fetchDatabase(remoteName).database
            if (!validateForeignDatabase(remote)) {
                return
            }

            DartsDatabaseUtil.swapInDatabase(remote)
        } catch (e: Exception) {
            handleSyncError(e, CODE_PULL_ERROR)
        } finally {
            tidyUpAllSyncDirs()
            SwingUtilities.invokeLater { DialogUtil.dismissLoadingDialogOLD() }
            ScreenCache.get<SyncManagementScreen>().initialise()
        }
    }

    fun doSyncIfNecessary(remoteName: String): Thread {
        if (needsSync()) {
            return doSync(remoteName)
        } else {
            logger.info(CODE_REVERT_TO_PULL, "Reverting to a pull as there are no local changes.")
            return doPull(remoteName)
        }
    }

    fun doSync(remoteName: String) = runInOtherThread { doSyncOnOtherThread(remoteName) }

    private fun doSyncOnOtherThread(remoteName: String) {
        try {
            val result = performSyncSteps(remoteName)
            if (result != null) {
                val summary =
                    "\n\nGames pushed: ${result.gamesPushed}\nGames pulled: ${result.gamesPulled}"
                DialogUtil.showInfoOLD("Sync completed successfully!$summary")
            }
        } catch (e: Exception) {
            handleSyncError(e, CODE_SYNC_ERROR)
        } finally {
            tidyUpAllSyncDirs()
            SyncProgressDialog.dispose()
            ScreenCache.get<SyncManagementScreen>().initialise()
        }
    }

    private fun performSyncSteps(remoteName: String): SyncResult? {
        setUpSyncDir()

        SyncProgressDialog.syncStarted()

        val fetchResult = dbStore.fetchDatabase(remoteName)

        SyncProgressDialog.progressToStage(SyncStage.VALIDATE_REMOTE)

        if (!validateForeignDatabase(fetchResult.database)) {
            return null
        }

        val localGamesToPush = getModifiedGameCount()
        val startingGameIds = getGameIds(mainDatabase)
        SyncProgressDialog.progressToStage(SyncStage.MERGE_LOCAL_CHANGES)

        val merger = DatabaseMerger(mainDatabase, fetchResult.database, remoteName)
        val resultingDatabase = merger.performMerge()

        val resultingGameIds = getGameIds(resultingDatabase)
        val deletedGameIds = getDeletedGameIds(resultingDatabase)
        checkAllGamesStillExist(startingGameIds, resultingGameIds, deletedGameIds)

        dbStore.pushDatabase(remoteName, resultingDatabase, fetchResult.lastModified)

        SyncProgressDialog.progressToStage(SyncStage.OVERWRITE_LOCAL)

        val success = DartsDatabaseUtil.swapInDatabase(resultingDatabase)
        SyncProgressDialog.dispose()

        if (!success) {
            return null
        }

        val gamesPulled = (resultingGameIds - startingGameIds).size
        return SyncResult(localGamesToPush, gamesPulled)
    }

    private fun validateForeignDatabase(db: Database): Boolean {
        val validator =
            ForeignDatabaseValidator(DatabaseMigrator(DatabaseMigrations.getConversionsMap()))
        return validator.validateAndMigrateForeignDatabase(db, "remote")
    }

    private fun getGameIds(database: Database) =
        GameEntity(database).retrieveModifiedSince(null).map { it.rowId }.toSet()

    private fun getDeletedGameIds(database: Database) =
        DeletionAuditEntity(database)
            .retrieveEntities("EntityName = 'Game'")
            .map { it.entityId }
            .toSet()

    private fun checkAllGamesStillExist(
        startingGameIds: Set<String>,
        resultingGameIds: Set<String>,
        deletedGameIds: Set<String>,
    ) {
        val missingGames = startingGameIds - resultingGameIds - deletedGameIds
        if (missingGames.isNotEmpty()) {
            throw SyncDataLossError(missingGames)
        }
    }

    private fun handleSyncError(e: Exception, code: LoggingCode) {
        when (e) {
            is SocketException,
            is InterruptedIOException -> {
                logger.warn(code, "Caught network error during sync: $e")
                DialogUtil.showErrorOLD(
                    "A connection error occurred. Check your internet connection and try again."
                )
            }
            is ConcurrentModificationException -> {
                logger.warn(code, "$e")
                DialogUtil.showErrorOLD(
                    "Another sync has been performed since this one started. \n\nResults have been discarded."
                )
            }
            is SyncDataLossError -> {
                logger.error(code, "$e", e, KEY_GAME_IDS to e.missingGameIds)
                DialogUtil.showErrorOLD(
                    "Sync resulted in missing data. \n\nResults have been discarded."
                )
            }
            is WrappedSqlException -> {
                logger.logSqlException(e.sqlStatement, e.genericStatement, e.sqlException)
                DialogUtil.showErrorOLD("An unexpected error occurred - no data has been changed.")
            }
            else -> {
                logger.error(code, "Unexpected error: $e", e)
                DialogUtil.showErrorOLD("An unexpected error occurred - no data has been changed.")
            }
        }
    }

    private fun setUpSyncDir() {
        tidyUpAllSyncDirs()
        File(SYNC_DIR).mkdirs()
    }

    private fun tidyUpAllSyncDirs() {
        File(SYNC_DIR).deleteRecursively()
        File("$databaseDirectory/${DartsDatabaseUtil.OTHER_DATABASE_NAME}").deleteRecursively()
    }
}
