package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.core.util.CoreRegistry.INSTANCE_STRING_DEVICE_ID
import dartzee.core.util.CoreRegistry.INSTANCE_STRING_USER_NAME
import dartzee.core.util.CoreRegistry.instance
import dartzee.helper.AbstractTest
import dartzee.helper.logger
import dartzee.logging.*
import dartzee.utils.DARTS_VERSION_NUMBER
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.maps.shouldContainAll
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.UIManager
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.plaf.nimbus.NimbusLookAndFeel

class TestMainUtil: AbstractTest()
{
    private val originalDeviceId = instance.get(INSTANCE_STRING_DEVICE_ID, "")
    private val originalUsername = instance.get(INSTANCE_STRING_USER_NAME, "")

    override fun afterEachTest()
    {
        instance.put(INSTANCE_STRING_DEVICE_ID, originalDeviceId)
        instance.put(INSTANCE_STRING_USER_NAME, originalUsername)
    }

    @Test
    fun `Should not attempt to set look and feel for Apple OS`()
    {
        UIManager.setLookAndFeel(MetalLookAndFeel())
        DartsClient.operatingSystem = "Mac"

        setLookAndFeel()

        UIManager.getLookAndFeel().shouldBeInstanceOf<MetalLookAndFeel>()
        errorLogged() shouldBe false
    }

    @Test
    fun `Should successfully set Nimbus look and feel for other OS`()
    {
        UIManager.setLookAndFeel(MetalLookAndFeel())
        DartsClient.operatingSystem = "Windows"

        setLookAndFeel()

        UIManager.getLookAndFeel().shouldBeInstanceOf<NimbusLookAndFeel>()
        verifyLog(CODE_LOOK_AND_FEEL_SET)
        errorLogged() shouldBe false
    }

    @Test
    fun `Should log an error if it fails to set look and feel`()
    {
        setLookAndFeel("invalid")

        val error = verifyLog(CODE_LOOK_AND_FEEL_ERROR, Severity.ERROR)
        error.message shouldContain "invalid"
        error.errorObject?.shouldBeInstanceOf<ClassNotFoundException>()
    }

    @Test
    fun `Should generate a device ID if not present, then return it in subsequent calls`()
    {
        instance.remove(INSTANCE_STRING_DEVICE_ID)

        val deviceId = getDeviceId()
        deviceId.shouldNotBeEmpty()

        instance.get(INSTANCE_STRING_DEVICE_ID, null) shouldBe deviceId
        getDeviceId() shouldBe deviceId
    }

    @Test
    fun `Should just return the value of the device ID in the registry if present`()
    {
        instance.put(INSTANCE_STRING_DEVICE_ID, "foo")
        getDeviceId() shouldBe "foo"
    }

    @Test
    fun `Should just return the value without showing a dialog if a username is present`()
    {
        instance.put(INSTANCE_STRING_USER_NAME, "some.user")

        getUsername() shouldBe "some.user"
        dialogFactory.inputsShown.shouldBeEmpty()
    }

    @Test
    fun `Should prompt for a username, set it and return it if no username is present`()
    {
        instance.remove(INSTANCE_STRING_USER_NAME)
        dialogFactory.inputSelection = "My Awesome Username"

        getUsername() shouldBe "My Awesome Username"
        dialogFactory.inputsShown.shouldContainExactly("Enter your name")
    }

    @Test
    fun `Should set up logging context fields`()
    {
        instance.put(INSTANCE_STRING_USER_NAME, "some.user")
        instance.put(INSTANCE_STRING_DEVICE_ID, "some.device")
        DartsClient.operatingSystem = "Windows 10"
        DartsClient.devMode = true

        setLoggingContextFields()

        val expectedContextFields = mapOf(KEY_USERNAME to "some.user", KEY_DEVICE_ID to "some.device",
                KEY_OPERATING_SYSTEM to "Windows 10", KEY_APP_VERSION to DARTS_VERSION_NUMBER, KEY_DEV_MODE to true)

        logger.loggingContext.shouldContainAll(expectedContextFields)
    }
}