package burlton.desktopcore.test.bean

import burlton.desktopcore.code.bean.RadioButtonPanel
import burlton.desktopcore.test.helpers.AbstractDesktopTest
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.Test
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JRadioButton

class TestRadioButtonPanel: AbstractDesktopTest()
{
    @Test
    fun `Should select the first radio button passed in by default`()
    {
        val rdbtnPanel = RadioButtonPanel()

        val rdbtn1 = JRadioButton()
        val rdbtn2 = JRadioButton()

        rdbtnPanel.add(rdbtn1)
        rdbtnPanel.add(rdbtn2)

        rdbtn1.isSelected shouldBe true
        rdbtn2.isSelected shouldBe false
    }

    @Test
    fun `Should only allow one radio selection at a time`()
    {
        val rdbtnPanel = RadioButtonPanel()

        val rdbtn1 = JRadioButton()
        val rdbtn2 = JRadioButton()

        rdbtnPanel.add(rdbtn1)
        rdbtnPanel.add(rdbtn2)

        rdbtn2.doClick()

        rdbtn1.isSelected shouldBe false
        rdbtn2.isSelected shouldBe true
    }

    @Test
    fun `Should return the selected radio button string`()
    {
        val rdbtnPanel = RadioButtonPanel()

        rdbtnPanel.getSelectionStr() shouldBe ""

        val rdbtn1 = JRadioButton("foo")
        val rdbtn2 = JRadioButton("bar")
        rdbtnPanel.add(rdbtn1)
        rdbtnPanel.add(rdbtn2)

        rdbtnPanel.getSelectionStr() shouldBe "foo"

        rdbtn2.doClick()
        rdbtnPanel.getSelectionStr() shouldBe "bar"
    }

    @Test
    fun `Should support setting the selection by string`()
    {
        val rdbtnPanel = RadioButtonPanel()

        val rdbtn1 = JRadioButton("foo")
        val rdbtn2 = JRadioButton("bar")

        rdbtnPanel.add(rdbtn1)
        rdbtnPanel.add(rdbtn2)

        rdbtn2.isSelected shouldBe false

        rdbtnPanel.setSelection("blergh")
        rdbtn2.isSelected shouldBe false
        rdbtnPanel.getSelectionStr() shouldBe "foo"

        rdbtnPanel.setSelection("bar")
        rdbtn2.isSelected shouldBe true
        rdbtnPanel.getSelectionStr() shouldBe "bar"
    }

    @Test
    fun `Should report whether it was the event source`()
    {
        val rdbtnPanel = RadioButtonPanel()

        val rdbtn1 = JRadioButton("foo")
        val rdbtn2 = JRadioButton("bar")
        val rdbtn3 = JRadioButton()

        rdbtnPanel.add(rdbtn1)
        rdbtnPanel.add(rdbtn2)

        rdbtnPanel.isEventSource(null) shouldBe false

        val ae = ActionEvent(rdbtn1, -1, "")
        rdbtnPanel.isEventSource(ae) shouldBe true

        ae.source = rdbtn2
        rdbtnPanel.isEventSource(ae) shouldBe true

        ae.source = rdbtn3
        rdbtnPanel.isEventSource(ae) shouldBe false
    }

    @Test
    fun `Should add action listeners to all radio buttons`()
    {
        val panel = RadioButtonPanel()

        val rdbtn1 = JRadioButton()
        val rdbtn2 = JRadioButton()
        panel.add(rdbtn1)
        panel.add(rdbtn2)

        val listener = SourceActionListener()
        panel.addActionListener(listener)

        rdbtn1.doClick()
        rdbtn2.doClick()

        listener.sources.shouldContainExactly(rdbtn1, rdbtn2)
    }

    class SourceActionListener: ActionListener
    {
        val sources = mutableListOf<Any>()

        override fun actionPerformed(e: ActionEvent?)
        {
            e ?: return

            sources.add(e.source)
        }
    }
}