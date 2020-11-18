package dartzee.sync

import dartzee.helper.AbstractTest
import dartzee.helper.usingInMemoryDatabase
import dartzee.utils.AwsUtils
import dartzee.utils.DartsDatabaseUtil.DATABASE_VERSION
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
        File("$SYNC_DIR/Databases").mkdirs()
        File("$SYNC_DIR/Databases/Test.txt").createNewFile()
        File("$SYNC_DIR/Databases/Test.txt").writeText(testFileText)
    }

    override fun afterEachTest()
    {
        super.afterEachTest()

        File(SYNC_DIR).deleteRecursively()
    }

    @Test
    fun `Should support pushing, checking existence and fetching the same database`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("aws-sync"))

        usingInMemoryDatabase(filePath = "$SYNC_DIR/Databases", withSchema = true) { db ->
            val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
            val remoteName = UUID.randomUUID().toString()
            store.pushDatabase(remoteName, db, null)

            store.databaseExists(remoteName) shouldBe true

            val resultingDatabase = store.fetchDatabase(remoteName).database
            resultingDatabase.filePath shouldBe "$SYNC_DIR/original/Databases"

            val copiedFile = File("$SYNC_DIR/original/Databases/Test.txt")
            copiedFile.shouldExist()
            copiedFile.readText() shouldBe testFileText
        }
    }

    @Test
    fun `Should return false for a remote database that does not exist`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("aws-sync"))

        val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
        store.databaseExists(UUID.randomUUID().toString()) shouldBe false
    }

    @Test
    fun `Should create a backup version, with filename including schema version`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("aws-sync"))

        val s3Client = AwsUtils.makeS3Client()

        usingInMemoryDatabase(filePath = "$SYNC_DIR/Databases", withSchema = true) { db ->
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
        Assume.assumeNotNull(AwsUtils.readCredentials("aws-sync"))

        usingInMemoryDatabase(filePath = "$SYNC_DIR/Databases", withSchema = true) { db ->
            val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
            val remoteName = UUID.randomUUID().toString()
            store.pushDatabase(remoteName, db, null)

            val lastModified = store.fetchDatabase(remoteName).lastModified

            Thread.sleep(500)

            // Make a change and push it again
            File("$SYNC_DIR/Databases/Test.txt").writeText("Modified text")
            store.pushDatabase(remoteName, db, null)

            val updatedModified = store.fetchDatabase(remoteName).lastModified

            val e = shouldThrow<ConcurrentModificationException> {
                store.pushDatabase(remoteName, db, lastModified)
            }

            e.message shouldBe "Remote database $remoteName was last modified $updatedModified, which is after $lastModified - aborting"
        }
    }
}