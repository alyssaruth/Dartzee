package dartzee.sync

import dartzee.core.util.DialogUtil
import dartzee.db.DatabaseMerger
import dartzee.db.DatabaseMigrator
import dartzee.db.SyncAuditEntity
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import java.io.File
import java.io.InterruptedIOException
import java.net.SocketException
import javax.swing.SwingUtilities

val SYNC_DIR = "${System.getProperty("user.dir")}/Sync"

class SyncManager(private val syncMode: SyncMode, private val remoteName: String, private val dbStore: IRemoteDatabaseStore)
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
            SwingUtilities.invokeLater { DialogUtil.showLoadingDialog("Performing sync...") }

            File(SYNC_DIR).deleteRecursively()
            File(SYNC_DIR).mkdirs()

            if (syncMode == SyncMode.CREATE_REMOTE)
            {
                SyncAuditEntity.insertSyncAudit(mainDatabase, remoteName)
                dbStore.pushDatabase(remoteName, mainDatabase)
            }
            else if (syncMode == SyncMode.OVERWRITE_LOCAL)
            {
                val remote = dbStore.fetchDatabase(remoteName).database
                SyncAuditEntity.insertSyncAudit(remote, remoteName)
                DartsDatabaseUtil.swapInDatabase(File(remote.filePath))
            }
            else
            {
                val fetchResult = dbStore.fetchDatabase(remoteName)
                val merger = makeDatabaseMerger(fetchResult.database, remoteName)
                if (!merger.validateMerge())
                {
                    return
                }

                val resultingDatabase = merger.performMerge()
                dbStore.pushDatabase(remoteName, resultingDatabase, fetchResult.lastModified)
                DartsDatabaseUtil.swapInDatabase(File(resultingDatabase.filePath))
            }

            saveRemoteName(remoteName)

            SwingUtilities.invokeLater {
                DialogUtil.dismissLoadingDialog()
                DialogUtil.showInfo("Sync completed successfully!")
            }

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
            SwingUtilities.invokeLater { DialogUtil.dismissLoadingDialog() }
            refreshSyncSummary()
        }
    }


    private fun makeDatabaseMerger(remoteDatabase: Database, remoteName: String)
      = DatabaseMerger(mainDatabase, remoteDatabase, DatabaseMigrator(DatabaseMigrations.getConversionsMap()), remoteName)
}