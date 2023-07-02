package dartzee.screen

import com.github.alyssaburlton.swingtest.getChild
import dartzee.helper.AbstractTest
import dartzee.utils.DARTS_VERSION_NUMBER
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import javax.swing.JTextArea

class TestChangeLog: AbstractTest()
{
    @Test
    fun `Should include the current version number`()
    {
        val changeLog = ChangeLog()

        val contents = changeLog.getChild<JTextArea>().text
        contents.lines().first() shouldContain DARTS_VERSION_NUMBER
    }
}