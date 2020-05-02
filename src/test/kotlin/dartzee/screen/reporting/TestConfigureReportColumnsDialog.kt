package dartzee.screen.reporting

import dartzee.clickComponent
import dartzee.core.util.getAllChildComponentsForType
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
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
        dlg.clickComponent<JCheckBox>("Type")
        dlg.clickComponent<JCheckBox>("Match")

        dlg.excludedColumns().shouldContainExactly("Type", "Match")
    }
}