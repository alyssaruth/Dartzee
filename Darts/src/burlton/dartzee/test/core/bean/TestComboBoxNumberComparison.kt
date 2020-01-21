package burlton.dartzee.test.core.bean

import burlton.dartzee.code.core.bean.ComboBoxNumberComparison
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Test

class TestComboBoxNumberComparison: AbstractTest()
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