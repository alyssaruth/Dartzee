package dartzee.screen.preference

import com.github.alyssaburlton.swingtest.clickChild
import com.github.alyssaburlton.swingtest.getChild
import com.github.alyssaburlton.swingtest.shouldBeDisabled
import com.github.alyssaburlton.swingtest.shouldBeEnabled
import dartzee.helper.AbstractRegistryTest
import javax.swing.JButton
import org.junit.jupiter.api.Test

abstract class AbstractPreferencePanelTest<T : AbstractPreferencesPanel> : AbstractRegistryTest() {
    abstract fun checkUiFieldValuesAreDefaults(panel: T)

    abstract fun checkUiFieldValuesAreNonDefaults(panel: T)

    abstract fun setUiFieldValuesToNonDefaults(panel: T)

    abstract fun checkPreferencesAreSetToNonDefaults()

    abstract fun factory(): T

    @Test
    fun `should restore defaults appropriately`() {
        val panel = factory()

        setUiFieldValuesToNonDefaults(panel)

        panel.refresh(true)

        checkUiFieldValuesAreDefaults(panel)
    }

    @Test
    fun `should set fields to their defaults when first loaded`() {
        clearPreferences()

        val panel = factory()
        panel.refresh(false)

        checkUiFieldValuesAreDefaults(panel)
    }

    @Test
    fun `should save preferences appropriately`() {
        clearPreferences()

        val panel = factory()
        setUiFieldValuesToNonDefaults(panel)
        panel.clickChild<JButton>(text = "Apply")

        checkPreferencesAreSetToNonDefaults()
    }

    @Test
    fun `should display stored preferences correctly`() {
        val panel = factory()
        setUiFieldValuesToNonDefaults(panel)
        panel.clickChild<JButton>(text = "Apply")

        panel.refresh(true)
        panel.refresh(false)

        checkUiFieldValuesAreNonDefaults(panel)
    }

    @Test
    fun `apply button should be disabled by default`() {
        val panel = factory()
        panel.refresh(false)

        panel.getChild<JButton>(text = "Apply").shouldBeDisabled()
    }

    @Test
    fun `apply button should respond to UI changes correctly`() {
        clearPreferences()

        val panel = factory()
        panel.refresh(false)
        panel.getChild<JButton>(text = "Apply").shouldBeDisabled()

        panel.clickChild<JButton>(text = "Restore Defaults")
        panel.getChild<JButton>(text = "Apply").shouldBeDisabled()

        setUiFieldValuesToNonDefaults(panel)
        panel.getChild<JButton>(text = "Apply").shouldBeEnabled()

        panel.clickChild<JButton>(text = "Restore Defaults")
        panel.getChild<JButton>(text = "Apply").shouldBeDisabled()

        setUiFieldValuesToNonDefaults(panel)
        panel.clickChild<JButton>(text = "Apply")
        panel.getChild<JButton>(text = "Apply").shouldBeDisabled()

        panel.clickChild<JButton>(text = "Restore Defaults")
        panel.getChild<JButton>(text = "Apply").shouldBeEnabled()

        setUiFieldValuesToNonDefaults(panel)
        panel.getChild<JButton>(text = "Apply").shouldBeDisabled()
    }
}
