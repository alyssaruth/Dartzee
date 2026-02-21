package dartzee.main

import dartzee.helper.AbstractTest
import dartzee.helper.logger
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
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.InjectedThings.preferenceService
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeInstanceOf
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
        UIManager.setLookAndFeel(MetalLookAndFeel())
        DartsClient.operatingSystem = "Windows"

        setLookAndFeel()

        UIManager.getLookAndFeel().shouldBeInstanceOf<NimbusLookAndFeel>()
        verifyLog(CODE_LOOK_AND_FEEL_SET)
        errorLogged() shouldBe false
    }

    @Test
    fun `Should log an error if it fails to set look and feel`() {
        setLookAndFeel("invalid")

        val error = verifyLog(CODE_LOOK_AND_FEEL_ERROR, Severity.ERROR)
        error.message shouldContain "invalid"
        error.errorObject?.shouldBeInstanceOf<ClassNotFoundException>()
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
}
