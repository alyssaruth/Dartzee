package burlton.desktopcore.test.bean

import burlton.desktopcore.code.bean.ComboBoxNumberComparison
import burlton.desktopcore.test.helper.AbstractDesktopTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestComboBoxNumberComparison: AbstractDesktopTest()
{
    @Test
    fun `Should contain the default options, and support adding new ones`()
    {
        val cb = ComboBoxNumberComparison()
        cb.itemCount shouldBe 3

        cb.addOption("foo")
        cb.itemCount shouldBe 4
        cb.getItemAt(3) shouldBe "foo"
    }
}