package dartzee.sync

import dartzee.helper.AbstractTest
import dartzee.helper.usingInMemoryDatabase
import dartzee.utils.AwsUtils
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.shouldBe
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
        AmazonS3RemoteDatabaseStore("dartzee-unit-test")
    }

    @Test
    fun `Should support pushing, checking existence and fetching the same database`()
    {
        Assume.assumeNotNull(AwsUtils.readCredentials("aws-sync"))

        usingInMemoryDatabase(filePath = "$SYNC_DIR/Databases", withSchema = true) { db ->
            val store = AmazonS3RemoteDatabaseStore("dartzee-unit-test")
            val remoteName = UUID.randomUUID().toString()
            store.pushDatabase(remoteName, db)

            store.databaseExists(remoteName) shouldBe true

            val resultingDatabase = store.fetchDatabase(remoteName)
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
}