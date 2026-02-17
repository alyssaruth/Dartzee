package dartzee.core.screen

import com.github.alyssaburlton.swingtest.clickOk
import com.github.alyssaburlton.swingtest.shouldNotBeVisible
import dartzee.core.bean.ScrollTable
import dartzee.core.util.TableUtil
import dartzee.core.util.getAllChildComponentsForType
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestTableModelDialog : AbstractTest() {
    @Test
    fun `Should display with the correct title and dialog properties`() {
        val table = ScrollTable()
        val tmd = TableModelDialog("Bah", table)

        tmd.title shouldBe "Bah"
        tmd.allowCancel() shouldBe false

        tmd.getAllChildComponentsForType<ScrollTable>().shouldContainExactly(table)
    }

    @Test
    fun `Should pass through column widths to the ScrollTable`() {
        val st = ScrollTable()
        val model = TableUtil.DefaultModel()
        model.addColumn("")
        st.model = model

        val tmd = TableModelDialog("Test", st)

        tmd.setColumnWidths("53")

        st.getColumn(0).width shouldBe 53
    }

    @Test
    fun `Should dispose when Ok is pressed`() {
        val tmd = TableModelDialog("Test", ScrollTable())
        tmd.isVisible = true

        tmd.clickOk()

        tmd.shouldNotBeVisible()
    }
}
