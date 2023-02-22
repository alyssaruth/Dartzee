package dartzee.core.bean

import com.github.alyssaburlton.swingtest.doLoseFocus
import dartzee.core.helper.processKeyPress
import dartzee.core.util.getAllChildComponentsForType
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.awt.BorderLayout
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.JComboBox
import javax.swing.JTextField

class TestSwingExtensions: AbstractTest()
{
    @Test
    fun `Should return the typed items of a combo box`()
    {
        val comboBox = JComboBox<String>()
        comboBox.items().shouldBeEmpty()
        comboBox.items().shouldBeInstanceOf<List<String>>()

        comboBox.addItem("Foo")
        comboBox.items().shouldContainExactly("Foo")

        comboBox.addItem("Bar")
        comboBox.items().shouldContainExactly("Foo", "Bar")
        comboBox.items().shouldBeInstanceOf<List<String>>()
    }

    @Test
    fun `Should return a typed selected item`()
    {
        val comboBox = JComboBox<String>()
        comboBox.addItem("Foo")
        comboBox.addItem("Bar")

        comboBox.selectedIndex = 0
        val selection: String = comboBox.selectedItemTyped()
        selection shouldBe "Foo"

        comboBox.selectedIndex = 1
        val otherSelection: String = comboBox.selectedItemTyped()
        otherSelection shouldBe "Bar"
    }

    @Test
    fun `Should find items by class`()
    {
        val comboBox = JComboBox<Number>()
        comboBox.addItem(20)
        comboBox.addItem(30.0)

        val item: Int? = comboBox.findByClass<Int>()
        item shouldBe 20

        val double: Double? = comboBox.findByClass<Double>()
        double shouldBe 30.0

        val long: Long? = comboBox.findByClass<Long>()
        long shouldBe null
    }

    @Test
    fun `Should select by class`()
    {
        val comboBox = JComboBox<Number>()
        comboBox.addItem(20)
        comboBox.addItem(30.0)

        comboBox.selectByClass<Double>()
        comboBox.selectedItem shouldBe 30.0

        comboBox.selectByClass<Int>()
        comboBox.selectedItem shouldBe 20
    }

    @Test
    fun `Should add a listener for when focus is lost`()
    {
        val actionListener = CapturingActionListener()
        val tf = JTextField()

        tf.addUpdateListener(actionListener)
        tf.doLoseFocus()

        actionListener.eventSource shouldBe tf
    }

    @Test
    fun `Should add an action for the specified key, and only process if a row is selected`()
    {
        val scrollTable = ScrollTable()

        var keyPressed = false
        scrollTable.addKeyAction(KeyEvent.VK_DELETE) { keyPressed = true }

        scrollTable.processKeyPress(KeyEvent.VK_ENTER)
        keyPressed shouldBe false

        scrollTable.processKeyPress(KeyEvent.VK_DELETE)
        keyPressed shouldBe false

        //Now select a row
        scrollTable.addColumn("Test")
        scrollTable.addRow(arrayOf("Foo"))
        scrollTable.selectFirstRow()

        scrollTable.processKeyPress(KeyEvent.VK_DELETE)
        keyPressed shouldBe true
    }

    @Test
    fun `Should add ghost text to a text component`()
    {
        val tf = JTextField()
        tf.addGhostText("Hello")

        tf.layout.shouldBeInstanceOf<BorderLayout>()

        val ghostText = tf.getAllChildComponentsForType<GhostText>().first()
        ghostText.text shouldBe "Hello"
    }

    private class CapturingActionListener: ActionListener
    {
        var eventSource: Any? = null

        override fun actionPerformed(e: ActionEvent?)
        {
            eventSource = e?.source
        }
    }

    @Test
    fun `Should return all of the points for a given width and height in the correct order`()
    {
        val points = getPointList(2, 3)
        points.shouldContainExactly(Point(0, 0), Point(1, 0), Point(0, 1), Point(1, 1), Point(0, 2), Point(1, 2))
    }
}