package dartzee.utils

import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
        newOut.toString().shouldContain("NullPointerException")
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