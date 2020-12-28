package dartzee.core.bean

import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

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