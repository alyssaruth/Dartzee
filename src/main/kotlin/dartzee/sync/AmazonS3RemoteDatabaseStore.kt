package dartzee.sync

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import dartzee.core.util.getFileTimeString
import dartzee.logging.*
import dartzee.utils.AwsUtils
import dartzee.utils.Database
import dartzee.utils.InjectedThings.logger
import net.lingala.zip4j.ZipFile
import java.io.File

class AmazonS3RemoteDatabaseStore(private val bucketName: String): IRemoteDatabaseStore
{
    private val credentials = AwsUtils.readCredentials("aws-sync")
    private val s3Client = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(credentials)).build()

    override fun databaseExists(name: String) = s3Client.doesObjectExist(bucketName, getCurrentDatabaseKey(name))

    override fun fetchDatabase(name: String): Database
    {
        logger.info(CODE_FETCHING_DATABASE, "Fetching database $name", KEY_REMOTE_NAME to name)

        val downloadPath = File("$SYNC_DIR/original.zip")
        val request = GetObjectRequest(bucketName, getCurrentDatabaseKey(name))
        s3Client.getObject(request, downloadPath)

        logger.info(CODE_FETCHED_DATABASE, "Fetched database $name - saved to $downloadPath", KEY_REMOTE_NAME to name)

        ZipFile(downloadPath).extractAll("$SYNC_DIR/original")

        logger.info(CODE_UNZIPPED_DATABASE, "Unzipped database $name to $SYNC_DIR/original")
        return Database("$SYNC_DIR/original/Databases")
    }

    override fun pushDatabase(name: String, database: Database)
    {
        logger.info(CODE_PUSHING_DATABASE, "Pushing database to $name", KEY_REMOTE_NAME to name)

        val dbVersion = database.getDatabaseVersion()
        val backupName = "${getFileTimeString()}_V$dbVersion.zip"

        val dbDirectory = File(database.filePath)
        val zipFilePath = File("$SYNC_DIR/new.zip")
        val zip = ZipFile(zipFilePath).also { it.addFolder(dbDirectory) }

        logger.info(CODE_ZIPPED_DATABASE, "Zipped up database to push to $name - $zipFilePath")

        s3Client.putObject(bucketName, getCurrentDatabaseKey(name), zip.file)
        logger.info(CODE_PUSHED_DATABASE, "Pushed database to $name/current.zip", KEY_REMOTE_NAME to name)

        s3Client.putObject(bucketName, "$name/backups/$backupName", zip.file)
        logger.info(CODE_PUSHED_DATABASE_BACKUP, "Pushed backup to $name/backups/$backupName", KEY_REMOTE_NAME to name)
    }

    private fun getCurrentDatabaseKey(remoteName: String) = "$remoteName/current.zip"
}