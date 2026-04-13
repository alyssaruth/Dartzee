package dartzee.main

import dartzee.game.GameType
import dartzee.getDialogMessage
import dartzee.getErrorDialog
import dartzee.helper.AbstractTest
import dartzee.helper.logger
import dartzee.helper.makeTheme
import dartzee.logging.CODE_LOOK_AND_FEEL_ERROR
import dartzee.logging.CODE_LOOK_AND_FEEL_SET
import dartzee.logging.KEY_APP_VERSION
import dartzee.logging.KEY_DEVICE_ID
import dartzee.logging.KEY_DEV_MODE
import dartzee.logging.KEY_OPERATING_SYSTEM
import dartzee.logging.KEY_USERNAME
import dartzee.logging.Severity
import dartzee.`object`.DartsClient
import dartzee.preferences.Preferences
import dartzee.runAsync
import dartzee.screen.animation.Animation
import dartzee.screen.animation.BRUCEY_BONUS
import dartzee.screen.animation.BULLSEYE_DEV
import dartzee.screen.animation.BadLuckTrigger
import dartzee.screen.animation.BruceyBonusTrigger
import dartzee.screen.animation.TotalScoreTrigger
import dartzee.theme.ThemeId
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings
import dartzee.utils.InjectedThings.preferenceService
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
import java.time.LocalDate
import javax.swing.UIManager
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.plaf.nimbus.NimbusLookAndFeel
import org.junit.jupiter.api.Test

class TestMainUtil : AbstractTest() {
    @Test
    fun `Should not attempt to set look and feel for Apple OS`() {
        UIManager.setLookAndFeel(MetalLookAndFeel())
        DartsClient.operatingSystem = "Mac"

        setLookAndFeel()

        UIManager.getLookAndFeel().shouldBeInstanceOf<MetalLookAndFeel>()
        errorLogged() shouldBe false
    }

    @Test
    fun `Should successfully set Nimbus look and feel for other OS`() {
        InjectedThings.now = LocalDate.of(2026, 1, 1)
        preferenceService.save(Preferences.theme, ThemeId.None)

        UIManager.setLookAndFeel(MetalLookAndFeel())
        DartsClient.operatingSystem = "Windows"

        setLookAndFeel()

        UIManager.getLookAndFeel().shouldBeInstanceOf<NimbusLookAndFeel>()
        verifyLog(CODE_LOOK_AND_FEEL_SET)
        errorLogged() shouldBe false
        InjectedThings.theme shouldBe null
    }

    @Test
    fun `Should log an error if it fails to set look and feel`() {
        InjectedThings.now = LocalDate.of(2026, 1, 1)
        preferenceService.save(Preferences.theme, ThemeId.None)

        runAsync { setLookAndFeel("invalid") }

        val error = verifyLog(CODE_LOOK_AND_FEEL_ERROR, Severity.ERROR)
        error.message shouldContain "invalid"
        error.errorObject?.shouldBeInstanceOf<ClassNotFoundException>()

        val errorDialog = getErrorDialog()
        errorDialog.getDialogMessage() shouldBe "Failed to load Look & Feel 'Nimbus'."
    }

    @Test
    fun `Should generate a device ID if not present, then return it in subsequent calls`() {

        val deviceId = getDeviceId()
        deviceId.shouldNotBeEmpty()

        preferenceService.get(Preferences.deviceId) shouldBe deviceId
        getDeviceId() shouldBe deviceId
    }

    @Test
    fun `Should just return the value of the device ID in the registry if present`() {
        preferenceService.save(Preferences.deviceId, "foo")
        getDeviceId() shouldBe "foo"
    }

    @Test
    fun `Should just return the value for the corresponding system property`() {
        System.setProperty("user.name", "some.user")

        getUsername() shouldBe "some.user"
    }

    @Test
    fun `Should set up logging context fields`() {
        System.setProperty("user.name", "some.user")
        preferenceService.save(Preferences.deviceId, "some.device")
        DartsClient.operatingSystem = "Windows 10"
        DartsClient.devMode = true

        setLoggingContextFields()

        val expectedContextFields =
            mapOf(
                KEY_USERNAME to "some.user",
                KEY_DEVICE_ID to "some.device",
                KEY_OPERATING_SYSTEM to "Windows 10",
                KEY_APP_VERSION to DARTS_VERSION_NUMBER,
                KEY_DEV_MODE to true,
            )

        logger.loggingContext.shouldContainAll(expectedContextFields)
    }

    @Test
    fun `Should merge default animations with theme, and initialise resource cache correctly`() {
        val lagerTrigger = TotalScoreTrigger(GameType.X01, 9)
        val lagerAnimation =
            Animation("nine-pints", "/theme/oktoberfest/horrific/pints-of-lager.png")
        val customAnimations = mapOf(BadLuckTrigger to BULLSEYE_DEV, lagerTrigger to lagerAnimation)

        InjectedThings.theme = makeTheme(animations = customAnimations)

        initialiseAnimations()

        val allAnimations = InjectedThings.animations
        allAnimations[BadLuckTrigger] shouldBe BULLSEYE_DEV
        allAnimations[lagerTrigger] shouldBe lagerAnimation
        allAnimations[BruceyBonusTrigger] shouldBe BRUCEY_BONUS
    }
}
