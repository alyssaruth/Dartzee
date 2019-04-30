package burlton.dartzee.test.screen

import burlton.core.code.util.OnlineConstants
import burlton.dartzee.code.screen.ChangeLog
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.string.shouldContain
import org.junit.Test

class TestChangeLog: AbstractDartsTest()
{
    @Test
    fun `Should include the current version number`()
    {
        val changeLog = ChangeLog()

        val contents = changeLog.textArea.text
        contents.lines().first() shouldContain OnlineConstants.DARTS_VERSION_NUMBER
    }
}