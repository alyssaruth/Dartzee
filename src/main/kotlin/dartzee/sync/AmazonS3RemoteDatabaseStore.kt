package dartzee.sync

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import dartzee.utils.AwsUtils
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import net.lingala.zip4j.ZipFile
import java.io.File

val SYNC_DIR = "${System.getProperty("user.dir")}\\Sync"

class AmazonS3RemoteDatabaseStore(private val bucketName: String): IRemoteDatabaseStore
{
    val credentials = AwsUtils.readCredentials("aws-sync")
    val s3Client = AmazonS3ClientBuilder.standard().withCredentials(AWSStaticCredentialsProvider(credentials)).build()

    override fun databaseExists(name: String) = s3Client.doesObjectExist(bucketName, name)

    override fun fetchDatabase(name: String): Database
    {
        return mainDatabase
    }

    override fun pushDatabase(name: String, database: Database)
    {
        val dbDirectory = File(database.filePath)
        File(SYNC_DIR).mkdirs()
        val zipFilePath = File("$SYNC_DIR/current.zip")

        val zip = ZipFile(zipFilePath).also { it.addFolder(dbDirectory) }
        s3Client.putObject(bucketName, name, zip.file)
    }
}