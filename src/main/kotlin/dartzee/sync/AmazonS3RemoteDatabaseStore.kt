package dartzee.sync

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import dartzee.core.util.getFileTimeString
import dartzee.utils.AwsUtils
import dartzee.utils.Database
import net.lingala.zip4j.ZipFile
import java.io.File

class AmazonS3RemoteDatabaseStore(private val bucketName: String): IRemoteDatabaseStore
{
    private val credentials = AwsUtils.readCredentials("aws-sync")
    private val s3Client = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(credentials)).build()

    override fun databaseExists(name: String) = s3Client.doesObjectExist(bucketName, "$name/current.zip")

    override fun fetchDatabase(name: String): Database
    {
        val downloadPath = File("$SYNC_DIR/original.zip")
        val request = GetObjectRequest(bucketName, "$name/current.zip")
        s3Client.getObject(request, downloadPath)

        ZipFile(downloadPath).extractAll("$SYNC_DIR/original")

        return Database("$SYNC_DIR/original/Databases")
    }

    override fun pushDatabase(name: String, database: Database)
    {
        val dbVersion = database.getDatabaseVersion()
        val backupName = "${getFileTimeString()}_V$dbVersion.zip"

        val dbDirectory = File(database.filePath)
        val zipFilePath = File("$SYNC_DIR/new.zip")

        val zip = ZipFile(zipFilePath).also { it.addFolder(dbDirectory) }
        s3Client.putObject(bucketName, "$name/current.zip", zip.file)
        s3Client.putObject(bucketName, "$name/backups/$backupName", zip.file)
    }
}