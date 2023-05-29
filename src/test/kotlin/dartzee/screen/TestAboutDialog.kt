package dartzee.screen

import com.github.alyssaburlton.swingtest.doClick
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeVisible
import dartzee.findWindow
import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import javax.swing.JLabel

class TestAboutDialog: AbstractTest()
{
    @Test
    fun `Should launch the ChangeLog if the link is clicked`()
    {
        val dlg = AboutDialog()
        dlg.isVisible = true

        val lbl = dlg.getChild<JLabel> { it.text.contains("Change Log") }
        lbl.doClick()

        dlg.isVisible shouldBe false

        val changeLog = findWindow<ChangeLog>()
        changeLog.shouldNotBeNull()
        changeLog.shouldBeVisible()
    }
}