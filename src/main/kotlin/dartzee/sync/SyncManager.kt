package dartzee.sync

import dartzee.core.util.DialogUtil
import dartzee.db.DatabaseMerger
import dartzee.db.DatabaseMigrator
import dartzee.screen.sync.SyncProgressDialog
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import java.io.File
import java.io.InterruptedIOException
import java.net.SocketException

val SYNC_DIR = "${System.getProperty("user.dir")}/Sync"

class SyncManager(private val remoteName: String, private val dbStore: IRemoteDatabaseStore)
{
    fun doSync()
    {
        val r = { doSyncOnOtherThread() }
        val t = Thread(r)
        t.start()
    }

    private fun doSyncOnOtherThread()
    {
        try
        {
            File(SYNC_DIR).deleteRecursively()
            File(SYNC_DIR).mkdirs()

            SyncProgressDialog.syncStarted()

            val fetchResult = dbStore.fetchDatabase(remoteName)
            val merger = makeDatabaseMerger(fetchResult.database, remoteName)
            if (!merger.validateMerge())
            {
                return
            }

            SyncProgressDialog.progressToStage(SyncStage.MERGE_LOCAL_CHANGES)

            val resultingDatabase = merger.performMerge()
            dbStore.pushDatabase(remoteName, resultingDatabase, fetchResult.lastModified)

            SyncProgressDialog.progressToStage(SyncStage.OVERWRITE_LOCAL)

            DartsDatabaseUtil.swapInDatabase(File(resultingDatabase.filePath))

            saveRemoteName(remoteName)

            SyncProgressDialog.dispose()
            DialogUtil.showInfo("Sync completed successfully!")
        }
        catch (e: Exception)
        {
            when (e)
            {
                is SocketException, is InterruptedIOException ->
                    DialogUtil.showError("A connection error occurred during database sync. Check your internet connection and try again.")
                else -> DialogUtil.showError("An unexpected error occurred during database sync. No data has been changed.")
            }

            throw e
        }
        finally
        {
            File(SYNC_DIR).deleteRecursively()
            SyncProgressDialog.dispose()
            refreshSyncSummary()
        }
    }


    private fun makeDatabaseMerger(remoteDatabase: Database, remoteName: String)
      = DatabaseMerger(mainDatabase, remoteDatabase, DatabaseMigrator(DatabaseMigrations.getConversionsMap()), remoteName)
}