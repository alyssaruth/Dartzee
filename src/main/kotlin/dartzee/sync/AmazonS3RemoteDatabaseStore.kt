package dartzee.sync

import com.amazonaws.services.s3.model.GetObjectRequest
import dartzee.core.util.getFileTimeString
import dartzee.logging.*
import dartzee.screen.sync.SyncProgressDialog
import dartzee.utils.AwsUtils
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File
import java.util.*
import kotlin.ConcurrentModificationException

class AmazonS3RemoteDatabaseStore(private val bucketName: String): IRemoteDatabaseStore
{
    private val s3Client = AwsUtils.makeS3Client()

    override fun databaseExists(remoteName: String) = s3Client.doesObjectExist(bucketName, getCurrentDatabaseKey(remoteName))

    override fun fetchDatabase(remoteName: String): FetchDatabaseResult
    {
        logger.info(CODE_FETCHING_DATABASE, "Fetching database $remoteName", KEY_REMOTE_NAME to remoteName)

        val downloadPath = File("$SYNC_DIR/original.zip")
        val request = GetObjectRequest(bucketName, getCurrentDatabaseKey(remoteName))
        val s3Obj = s3Client.getObject(request, downloadPath)

        logger.info(CODE_FETCHED_DATABASE,
            "Fetched database $remoteName - saved to $downloadPath. Last modified remotely: ${s3Obj.lastModified}",
            KEY_REMOTE_NAME to remoteName)

        val resultingDb = Database(DartsDatabaseUtil.OTHER_DATABASE_NAME)
        ZipFile(downloadPath).extractAll(resultingDb.getDirectoryStr())
        logger.info(CODE_UNZIPPED_DATABASE, "Unzipped database $remoteName to ${resultingDb.getDirectory()}")
        return FetchDatabaseResult(resultingDb, s3Obj.lastModified)
    }

    override fun pushDatabase(remoteName: String, database: Database, lastModified: Date?)
    {
        SyncProgressDialog.progressToStage(SyncStage.PUSH_TO_REMOTE)

        logger.info(CODE_PUSHING_DATABASE, "Pushing database to $remoteName", KEY_REMOTE_NAME to remoteName)

        lastModified?.let { verifyLastModifiedNotChanged(remoteName, lastModified) }

        val dbDirectory = database.getDirectory()

        val zipFilePath = File("$SYNC_DIR/new.zip")
        val params = ZipParameters().also { it.isIncludeRootFolder = false }
        val zip = ZipFile(zipFilePath).also { it.addFolder(dbDirectory, params) }

        logger.info(CODE_ZIPPED_DATABASE, "Zipped up database to push to $remoteName - $zipFilePath")

        s3Client.putObject(bucketName, getCurrentDatabaseKey(remoteName), zip.file)
        logger.info(CODE_PUSHED_DATABASE, "Pushed database to ${getCurrentDatabaseKey(remoteName)}", KEY_REMOTE_NAME to remoteName)

        SyncProgressDialog.progressToStage(SyncStage.PUSH_BACKUP_TO_REMOTE)

        val dbVersion = database.getDatabaseVersion()
        val backupName = "${getFileTimeString()}_V$dbVersion.zip"
        s3Client.copyObject(bucketName, getCurrentDatabaseKey(remoteName), bucketName, "$remoteName/backups/$backupName")
        logger.info(CODE_PUSHED_DATABASE_BACKUP, "Copied backup to $remoteName/backups/$backupName", KEY_REMOTE_NAME to remoteName)
    }

    private fun verifyLastModifiedNotChanged(remoteName: String, lastModified: Date)
    {
        val metadata = s3Client.getObjectMetadata(bucketName, getCurrentDatabaseKey(remoteName))
        if (metadata.lastModified > lastModified)
        {
            throw ConcurrentModificationException("Remote database $remoteName was last modified ${metadata.lastModified}, which is after $lastModified - aborting")
        }
    }

    private fun getCurrentDatabaseKey(remoteName: String) = "$remoteName/current.zip"
}