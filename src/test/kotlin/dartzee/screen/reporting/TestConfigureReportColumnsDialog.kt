package dartzee.screen.reporting

import com.github.alyssaburlton.swingtest.clickChild
import dartzee.core.util.getAllChildComponentsForType
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import javax.swing.JCheckBox

class TestConfigureReportColumnsDialog: AbstractTest()
{
    @Test
    fun `Should start with all options checked`()
    {
        val dlg = ConfigureReportColumnsDialog()
        val checkBoxes = dlg.getAllChildComponentsForType<JCheckBox>()
        checkBoxes.forEach { it.isSelected shouldBe true }

        dlg.excludedColumns().shouldBeEmpty()
    }

    @Test
    fun `Should exclude unticked options`()
    {
        val dlg = ConfigureReportColumnsDialog()
        dlg.clickChild<JCheckBox>(text = "Type")
        dlg.clickChild<JCheckBox>(text = "Match")

        dlg.excludedColumns().shouldContainExactly("Type", "Match")
    }
}