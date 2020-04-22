package dartzee.core.util

import dartzee.core.helper.exceptionLogged
import dartzee.core.helper.getLogs
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.junit.Test
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class TestComponentUtil: AbstractTest()
{
    @Test
    fun `Should return children of the appropriate type`()
    {
        val panel = JPanel()
        val btn = JButton()
        val rdbtn = JRadioButton()

        panel.add(btn)
        panel.add(rdbtn)

        panel.getAllChildComponentsForType<JButton>().shouldContainExactly(btn)
        panel.getAllChildComponentsForType<JRadioButton>().shouldContainExactly(rdbtn)
        panel.getAllChildComponentsForType<AbstractButton>().shouldContainExactly(btn, rdbtn)
        panel.getAllChildComponentsForType<JCheckBox>().shouldBeEmpty()
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

        panel.getAllChildComponentsForType<JButton>().shouldContainExactly(btn1, btn2, btn3)
    }

    @Test
    fun `Should identify whether a component is contained`()
    {
        val panel = JPanel()
        val panel2 = JPanel()
        val btnOne = JButton()

        panel.add(panel2)
        panel2.add(btnOne)

        panel.containsComponent(btnOne) shouldBe true
        panel2.containsComponent(btnOne) shouldBe true

        panel.containsComponent(JButton()) shouldBe false
    }

    @Test
    fun `Should not create an empty ButtonGroup`()
    {
        createButtonGroup()

        val logs = getLogs()
        exceptionLogged() shouldBe true
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

    @Test
    fun `Should return null if no parent window`()
    {
        val panel = JPanel()
        panel.getParentWindow() shouldBe null
    }

    @Test
    fun `Should recurse up the tree to find the parent window`()
    {
        val window = JFrame()
        val panel = JPanel()
        val btn = JButton()
        window.contentPane.add(panel)
        panel.add(btn)

        btn.getParentWindow() shouldBe window
        panel.getParentWindow() shouldBe window
    }

    @Test
    fun `Should enable and disable all nested components`()
    {
        val window = JFrame()
        val panel = JPanel()
        val btn = JButton()
        val subPanel = JPanel()
        val subBtn = JButton()

        window.contentPane.add(panel)
        panel.add(btn)
        panel.add(subPanel)
        subPanel.add(subBtn)

        window.enableChildren(false)
        btn.isEnabled shouldBe false
        subBtn.isEnabled shouldBe false

        window.enableChildren(true)
        btn.isEnabled shouldBe true
        subBtn.isEnabled shouldBe true
    }

    @Test
    fun `Should add actionListeners to all applicable children`()
    {
        val window = JFrame()
        val panel = JPanel()
        val btn = JButton()
        val subPanel = JPanel()
        val subBtn = JRadioButton()
        val subCombo = JComboBox<String>()

        window.contentPane.add(panel)
        panel.add(btn)
        panel.add(subPanel)
        subPanel.add(subBtn)
        subPanel.add(subCombo)

        val actionListener = mockk<ActionListener>()
        window.addActionListenerToAllChildren(actionListener)

        btn.actionListeners.toList().shouldContainExactly(actionListener)
        subBtn.actionListeners.toList().shouldContainExactly(actionListener)
        subCombo.actionListeners.toList().shouldContainExactly(actionListener)
    }

    @Test
    fun `Should not add the same actionListener twice`()
    {
        val panel = JPanel()
        val btn = JButton()

        panel.add(btn)

        val listenerOne = mockk<ActionListener>()
        val listenerTwo = mockk<ActionListener>()

        panel.addActionListenerToAllChildren(listenerOne)
        btn.actionListeners.toList().shouldContainExactly(listenerOne)

        panel.addActionListenerToAllChildren(listenerOne)
        btn.actionListeners.toList().shouldContainExactly(listenerOne)

        panel.addActionListenerToAllChildren(listenerTwo)
        btn.actionListeners.toList().shouldContainExactlyInAnyOrder(listenerOne, listenerTwo)
    }

    @Test
    fun `Should add changeListeners to all applicable children`()
    {
        val window = JFrame()
        val panel = JPanel()
        val spinner = JSpinner()
        val subPanel = JPanel()
        val subSpinner = JSpinner()

        window.contentPane.add(panel)
        panel.add(spinner)
        panel.add(subPanel)
        subPanel.add(subSpinner)

        val changeListener = ListenerOne()
        window.addChangeListenerToAllChildren(changeListener)

        spinner.changeListeners.toList().shouldContainExactlyInAnyOrder(changeListener, spinner.editor)
        subSpinner.changeListeners.toList().shouldContainExactlyInAnyOrder(changeListener, subSpinner.editor)
    }

    @Test
    fun `Should not add the same changeListener twice`()
    {
        val panel = JPanel()
        val spinner = JSpinner()

        panel.add(spinner)

        val listenerOne = ListenerOne()
        val listenerTwo = ListenerTwo()

        panel.addChangeListenerToAllChildren(listenerOne)
        spinner.changeListeners.toList().shouldContainExactlyInAnyOrder(listenerOne, spinner.editor)

        panel.addChangeListenerToAllChildren(listenerOne)
        spinner.changeListeners.toList().shouldContainExactlyInAnyOrder(listenerOne, spinner.editor)

        panel.addChangeListenerToAllChildren(listenerTwo)
        spinner.changeListeners.toList().shouldContainExactlyInAnyOrder(listenerOne, listenerTwo, spinner.editor)
    }

    private class ListenerOne: ChangeListener
    {
        override fun stateChanged(e: ChangeEvent?) {}
    }

    private class ListenerTwo: ChangeListener
    {
        override fun stateChanged(e: ChangeEvent?) {}
    }
}