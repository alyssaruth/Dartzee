package dartzee.e2e

import dartzee.helper.AbstractTest
import dartzee.preferences.Preferences
import dartzee.utils.InjectedThings.preferenceService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag

@Tag("e2e")
open class AbstractE2ETest : AbstractTest() {
    @BeforeEach
    open fun beforeEach() {
        preferenceService.save(Preferences.aiSpeed, 50)
        preferenceService.save(Preferences.aiAutoContinue, true)
        preferenceService.save(Preferences.showAnimations, false)
    }
}
