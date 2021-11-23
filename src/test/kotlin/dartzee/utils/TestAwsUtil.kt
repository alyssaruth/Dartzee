package dartzee.utils

import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class TestAwsUtil: AbstractTest()
{
    private val rsrcPath = File(javaClass.getResource("/ChangeLog")!!.file).absolutePath
    private val newRsrcPath = rsrcPath.replace("ChangeLog", "foo")
    private val testFile: File = File(newRsrcPath)
    private val originalOut = System.out
    private val newOut = ByteArrayOutputStream()

    @BeforeEach
    fun beforeEach()
    {
        System.setOut(PrintStream(newOut))
    }

    @AfterEach
    fun afterEach()
    {
        testFile.delete()
        System.setOut(originalOut)
    }

    @Test
    fun `Should print an error and return null if file does not exist`()
    {
        clearLogs()

        val credentials = AwsUtils.readCredentials("foo")
        credentials shouldBe null

        getLogRecords().shouldBeEmpty()
        newOut.toString().shouldContain("getenv(resourceName) must not be null")
    }

    @Test
    fun `Should print an error and return null for invalid file contents`()
    {
        testFile.writeText("foo")
        clearLogs()

        val credentials = AwsUtils.readCredentials("foo")
        credentials shouldBe null

        getLogRecords().shouldBeEmpty()
        newOut.toString().shouldContain("Failed to read in AWS credentials: java.lang.IndexOutOfBoundsException")
    }
}