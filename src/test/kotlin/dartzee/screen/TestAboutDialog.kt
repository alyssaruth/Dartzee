package dartzee.screen

import dartzee.doMouseMove
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings
import doClick
import find
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.awt.Cursor
import javax.swing.JLabel

class TestAboutDialog: AbstractTest()
{
    @Test
    fun `Should launch the ChangeLog if the link is clicked`()
    {
        InjectedThings.showChangeLog = mockk(relaxed = true)

        val dlg = AboutDialog()
        dlg.isVisible = true

        val lbl = dlg.find<JLabel>("Change Log")!!
        lbl.doClick()

        dlg.isVisible shouldBe false

        verify { InjectedThings.showChangeLog() }
    }

    @Test
    fun `Should treat the changelog label like a hyperlink`()
    {
        val dlg = AboutDialog()

        val lbl = dlg.find<JLabel>("Change Log")!!
        lbl.doMouseMove()

        dlg.cursor.type shouldBe Cursor.HAND_CURSOR
    }
}