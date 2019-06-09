package burlton.desktopcore.test.util

import burlton.core.code.util.AbstractClient
import burlton.core.test.helper.getLogs
import burlton.desktopcore.code.util.ClientEmailer
import burlton.desktopcore.code.util.ClientEmailer.TEMP_DIRECTORY
import burlton.desktopcore.code.util.LOG_FILENAME_PREFIX
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets

class TestClientEmailer: AbstractDesktopTest()
{
    override fun afterEachTest()
    {
        super.afterEachTest()

        File(ClientEmailer.TEMP_DIRECTORY).deleteRecursively()
    }

    @Test
    fun `Should not attempt to send email with no log secret`()
    {
        AbstractClient.logSecret = ""
        ClientEmailer.canSendEmail() shouldBe false

        AbstractClient.logSecret = "foo"
        ClientEmailer.canSendEmail() shouldBe true
    }

    @Test
    fun `Should write to a file if unable to send email`()
    {
        AbstractClient.logSecret = ""
        ClientEmailer.sendClientEmail("Title", "Body")

        val files = File(ClientEmailer.TEMP_DIRECTORY).listFiles()

        files.size shouldBe 1
        val f = files.first()
        f.name shouldStartWith(LOG_FILENAME_PREFIX)
        f.name shouldEndWith(".txt")

        val logStr = f.readText(StandardCharsets.UTF_8)
        logStr shouldBe "Title\nBody"
    }

    @Test
    fun `Should not send old logs if temp does not exist`()
    {
        ClientEmailer.tryToSendUnsentLogs()

        getLogs() shouldContain "${ClientEmailer.TEMP_DIRECTORY} does not exist, no logs to resend"
    }

    @Test
    fun `Should not attempt to send logs of the wrong name`()
    {
        val tempDir = File(ClientEmailer.TEMP_DIRECTORY)
        tempDir.mkdirs()

        val fileName = "$tempDir/foo.txt"
        File(fileName).writeText("Hello")

        ClientEmailer.tryToSendUnsentLogs()

        getLogs() shouldContain "There are no logs to resend in $TEMP_DIRECTORY"
    }

    @Test
    fun `Should not attempt to send logs of the wrong format`()
    {
        val tempDir = File(ClientEmailer.TEMP_DIRECTORY)
        tempDir.mkdirs()

        val fileName = "$tempDir/${LOG_FILENAME_PREFIX}_${System.currentTimeMillis()}.xml"
        File(fileName).writeText("Hello")

        ClientEmailer.tryToSendUnsentLogs()

        getLogs() shouldContain "There are no logs to resend in $TEMP_DIRECTORY"
    }
}