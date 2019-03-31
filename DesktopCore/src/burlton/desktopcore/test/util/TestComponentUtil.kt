package burlton.desktopcore.test.util

import burlton.core.code.util.Debug
import burlton.desktopcore.code.util.containsComponent
import burlton.desktopcore.code.util.createButtonGroup
import burlton.desktopcore.code.util.getAllChildComponentsForType
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.*

class TestComponentUtil: AbstractDesktopTest()
{
    @Test
    fun `Should return children of the appropriate type`()
    {
        val panel = JPanel()
        val btn = JButton()
        val rdbtn = JRadioButton()

        panel.add(btn)
        panel.add(rdbtn)

        getAllChildComponentsForType(panel, JButton::class.java).shouldContainExactly(btn)
        getAllChildComponentsForType(panel, JRadioButton::class.java).shouldContainExactly(rdbtn)
        getAllChildComponentsForType(panel, AbstractButton::class.java).shouldContainExactly(btn, rdbtn)
        getAllChildComponentsForType(panel, JCheckBox::class.java).shouldBeEmpty()
    }

    @Test
    fun `should return nested components`()
    {
        val panel = JPanel()
        val panel2 = JPanel()
        val panel3 = JPanel()

        val btn1 = JButton()
        val btn2 = JButton()
        val btn3 = JButton()

        panel.add(btn1)
        panel.add(panel2)
        panel2.add(btn2)
        panel2.add(panel3)
        panel3.add(btn3)

        getAllChildComponentsForType(panel, JButton::class.java).shouldContainExactly(btn1, btn2, btn3)
    }

    @Test
    fun `Should identify whether a component is contained`()
    {
        val panel = JPanel()
        val panel2 = JPanel()
        val btnOne = JButton()

        panel.add(panel2)
        panel2.add(btnOne)

        containsComponent(panel, btnOne) shouldBe true
        containsComponent(panel2, btnOne) shouldBe true

        containsComponent(panel, JButton()) shouldBe false
    }

    @Test
    fun `Should not create an empty ButtonGroup`()
    {
        Debug.clearLogs()

        createButtonGroup()

        Debug.waitUntilLoggingFinished()

        val logs = Debug.getLogs()
        logs shouldContain("Trying to create empty ButtonGroup")
        logs shouldContain("Debug.stackTrace")
    }

    @Test
    fun `Should create a working button group and select the first radiobutton passed in`()
    {
        val rdbtnOne = JRadioButton()
        val rdbtnTwo = JRadioButton()

        createButtonGroup(rdbtnOne, rdbtnTwo)

        rdbtnOne.isSelected.shouldBeTrue()
        rdbtnTwo.isSelected.shouldBeFalse()

        rdbtnTwo.isSelected = true

        rdbtnOne.isSelected.shouldBeFalse()
    }
}