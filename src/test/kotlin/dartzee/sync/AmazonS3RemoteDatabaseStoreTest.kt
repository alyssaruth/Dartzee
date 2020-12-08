package dartzee.sync

import dartzee.helper.AbstractTest
import dartzee.helper.usingInMemoryDatabase
import dartzee.utils.AwsUtils
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
import dartzee.utils.DartsDatabaseUtil.OTHER_DATABASE_NAME
import dartzee.utils.InjectedThings.databaseDirectory
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Assume
import org.junit.Test
import java.io.File
import java.util.*

class AmazonS3RemoteDatabaseStoreTest: AbstractTest()
{
    private val testFileText = "This isn't a database!"

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        File(SYNC_DIR).deleteRecursively()
        File(SYNC_DIR).mkdirs()
    }

    override fun afterEachTest()
    {
        super.afterEachTest()

        File(SYNC_DIR).deleteRecursively()
        File("${databaseDirectory}/$OTHER_DATABASE_NAME").deleteRecursively()
    }

    @Test
    fun `Should support pushing, checking existence and fetching the same database`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("AWS_SYNC"))

        usingInMemoryDatabase(withSchema = true) { db ->
            val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
            val remoteName = UUID.randomUUID().toString()

            val dbFile = File("${db.getDirectory()}/Test.txt")
            dbFile.createNewFile()
            dbFile.writeText(testFileText)

            store.pushDatabase(remoteName, db, null)

            store.databaseExists(remoteName) shouldBe true

            val resultingDatabase = store.fetchDatabase(remoteName).database
            resultingDatabase.dbName shouldBe OTHER_DATABASE_NAME

            val copiedFile = File("$databaseDirectory/$OTHER_DATABASE_NAME/Test.txt")
            copiedFile.shouldExist()
            copiedFile.readText() shouldBe testFileText
        }
    }

    @Test
    fun `Should return false for a remote database that does not exist`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("AWS_SYNC"))

        val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
        store.databaseExists(UUID.randomUUID().toString()) shouldBe false
    }

    @Test
    fun `Should create a backup version, with filename including schema version`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("AWS_SYNC"))

        val s3Client = AwsUtils.makeS3Client()

        usingInMemoryDatabase(withSchema = true) { db ->
            File("${db.getDirectory()}/Test.txt").createNewFile()

            val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
            val remoteName = UUID.randomUUID().toString()
            store.pushDatabase(remoteName, db, null)

            val objects = s3Client.listObjects("dartzee-unit-test", "$remoteName/backups/").objectSummaries
            objects.size shouldBe 1
            objects.first().key shouldContain "V${DATABASE_VERSION}.zip"
        }
    }

    @Test
    fun `Should throw an error if trying to overwrite database which has been modified since it was fetched`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("AWS_SYNC"))

        usingInMemoryDatabase(withSchema = true) { db ->
            val dbFile = File("${db.getDirectory()}/Test.txt")
            dbFile.createNewFile()

            val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
            val remoteName = UUID.randomUUID().toString()
            store.pushDatabase(remoteName, db, null)

            val lastModified = store.fetchDatabase(remoteName).lastModified

            Thread.sleep(1000)

            // Make a change and push it again
            dbFile.writeText("Modified text")
            store.pushDatabase(remoteName, db, null)

            val updatedModified = store.fetchDatabase(remoteName).lastModified

            val e = shouldThrow<ConcurrentModificationException> {
                store.pushDatabase(remoteName, db, lastModified)
            }

            e.message shouldBe "Remote database $remoteName was last modified $updatedModified, which is after $lastModified - aborting"
        }
    }
}