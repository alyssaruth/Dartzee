package dartzee.screen

import dartzee.screen.ChangeLog
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.string.shouldContain
import org.junit.Test

class TestChangeLog: AbstractTest()
{
    @Test
    fun `Should include the current version number`()
    {
        val changeLog = ChangeLog()

        val contents = changeLog.textArea.text
        contents.lines().first() shouldContain DARTS_VERSION_NUMBER
    }
}