package burlton.dartzee.test.screen.preference

import burlton.dartzee.code.screen.preference.AbstractPreferencesPanel
import burlton.dartzee.test.helper.AbstractRegistryTest
import org.junit.Test

abstract class AbstractPreferencePanelTest<T: AbstractPreferencesPanel>: AbstractRegistryTest()
{
    abstract fun checkUiFieldValuesAreDefaults(panel: T)
    abstract fun checkUiFieldValuesAreNonDefaults(panel: T)
    abstract fun setUiFieldValuesToNonDefaults(panel: T)
    abstract fun checkPreferencesAreSetToNonDefaults()
    abstract fun factory(): T

    @Test
    fun `should restore defaults appropriately`()
    {
        val panel = factory()

        setUiFieldValuesToNonDefaults(panel)

        panel.refresh(true)

        checkUiFieldValuesAreDefaults(panel)
    }

    @Test
    fun `should set fields to their defaults when first loaded`()
    {
        clearPreferences()

        val panel = factory()
        panel.refresh(false)

        checkUiFieldValuesAreDefaults(panel)
    }

    @Test
    fun `should save preferences appropriately`()
    {
        clearPreferences()

        val panel = factory()
        setUiFieldValuesToNonDefaults(panel)
        panel.save()

        checkPreferencesAreSetToNonDefaults()
    }

    @Test
    fun `should display stored preferences correctly`()
    {
        val panel = factory()
        setUiFieldValuesToNonDefaults(panel)
        panel.save()

        panel.refresh(true)
        panel.refresh(false)

        checkUiFieldValuesAreNonDefaults(panel)
    }
}