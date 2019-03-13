package burlton.dartzee.test.screen.preference

import burlton.dartzee.code.screen.preference.AbstractPreferencesPanel
import burlton.dartzee.test.helper.AbstractRegistryTest
import org.junit.Test

abstract class AbstractPreferencePanelTest<T: AbstractPreferencesPanel>: AbstractRegistryTest()
{
    abstract fun checkUiFieldValuesAreDefaults(panel: T)
    abstract fun setUiFieldValuesToNonDefaults(panel: T)
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
}