package dartzee.logging

import dartzee.helper.AbstractTest
import dartzee.logging.LoggerFactory.readCredentials
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class TestLoggerFactory: AbstractTest()
{
    private val originalFile = File(javaClass.getResource("/aws").toURI())
    private val zzFile = File("${originalFile.path}-moved")

    private val originalOut = System.out
    private val newOut = ByteArrayOutputStream()

    override fun beforeEachTest()
    {
        super.beforeEachTest()

        System.setOut(PrintStream(newOut))
        originalFile.renameTo(zzFile)
    }

    override fun afterEachTest()
    {
        super.afterEachTest()

        originalFile.delete()
        System.setOut(originalOut)
        zzFile.renameTo(originalFile)
    }

    @Test
    fun `Should print an error and return null if file does not exist`()
    {
        clearLogs()

        val credentials = readCredentials()
        credentials shouldBe null

        getLogRecords().shouldBeEmpty()
        newOut.toString().shouldContain("java.lang.IllegalStateException: javaClass.getResource(\"/aws\") must not be null")
    }

    @Test
    fun `Should print an error and return null for invalid file contents`()
    {
        originalFile.writeText("foo")
        clearLogs()

        val credentials = readCredentials()
        credentials shouldBe null

        getLogRecords().shouldBeEmpty()
        newOut.toString().shouldContain("Failed to read in AWS credentials: java.lang.IndexOutOfBoundsException")
    }

}