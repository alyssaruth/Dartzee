package dartzee.utils

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractTest
import dartzee.logging.CODE_EXEC_ERROR
import dartzee.logging.Severity
import dartzee.`object`.DartsClient
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import org.junit.jupiter.api.Test

class CommandUtilTest : AbstractTest() {
    @Test
    fun `launchUrl should execute the expected command on linux`() {
        DartsClient.operatingSystem = "linux"
        val runtime = mockk<Runtime>(relaxed = true)
        launchUrl("foo.bar", runtime)

        verify { runtime.exec(arrayOf("xdg-open", "foo.bar")) }
    }

    @Test
    fun `launchUrl should execute the expected command on Windows`() {
        DartsClient.operatingSystem = "windows"
        val runtime = mockk<Runtime>(relaxed = true)
        launchUrl("google.com", runtime)

        verify { runtime.exec(arrayOf("cmd", "/c", "start", "google.com")) }
    }

    @Test
    fun `launchUrl should log an appropriate error if launching the URL fails`() {
        DartsClient.operatingSystem = "linux"
        val error = IOException("Oops")

        val runtime = mockk<Runtime>()
        every { runtime.exec(any<Array<String>>()) }.throws(error)

        launchUrl("foo.bar", runtime)

        val log = verifyLog(CODE_EXEC_ERROR, Severity.ERROR)
        log.message shouldBe "Command failed: xdg-open foo.bar"
        log.errorObject shouldBe error
    }

    @Test
    fun `runCommand should error for unsupported OS`() {
        DartsClient.operatingSystem = "apple"
        val runtime = mockk<Runtime>()

        runCommand(windows = arrayOf(), linux = arrayOf())

        val log = verifyLog(CODE_EXEC_ERROR, Severity.ERROR)
        log.message shouldBe "Operating system unsupported: apple"

        verifyNotCalled { runtime.exec(any<Array<String>>()) }
    }
}
