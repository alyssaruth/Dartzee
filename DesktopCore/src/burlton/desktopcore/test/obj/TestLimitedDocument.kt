package burlton.desktopcore.test.obj

import burlton.desktopcore.code.obj.LimitedDocument
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestLimitedDocument: AbstractDesktopTest()
{
    @Test
    fun testLimitedDocument()
    {
        val limitedDocument = LimitedDocument(50)

        limitedDocument.insertString(0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", null) //40 chars
        limitedDocument.insertString(0, null, null) //Should do nothing
        limitedDocument.insertString(0, "bbbbbbbbbbb", null) //11 chars - too many
        limitedDocument.insertString(0, "cccccccccc", null) //10 chars - should work
        limitedDocument.insertString(0, "d", null) //Now full - shouldn't accept anything

        val text = limitedDocument.getText(0, 50)

        text.shouldBe("ccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
    }
}