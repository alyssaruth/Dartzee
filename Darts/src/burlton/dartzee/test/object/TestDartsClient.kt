package burlton.dartzee.test.`object`

import burlton.core.test.helper.getLogs
import burlton.dartzee.code.`object`.DartsClient
import burlton.dartzee.code.utils.DARTS_VERSION_NUMBER
import burlton.dartzee.code.utils.PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES
import burlton.dartzee.code.utils.PreferenceUtil
import burlton.dartzee.code.utils.UpdateManager
import burlton.dartzee.test.helper.AbstractRegistryTest
import burlton.dartzee.test.helper.verifyNotCalled
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class TestDartsClient: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES)

    @Test
    fun `Should parse and set logSecret if present`()
    {
        DartsClient.logSecret = ""
        DartsClient.parseProgramArguments(arrayOf("logSecret=foo"))

        DartsClient.logSecret shouldBe "foo"
        getLogs() shouldContain "logSecret is present - will email diagnostics"
    }

    @Test
    fun `Should fall back on an empty value for logSecret if passed without a value`()
    {
        DartsClient.logSecret = "foo"
        DartsClient.parseProgramArguments(arrayOf("logSecret"))

        DartsClient.logSecret shouldBe ""
        getLogs() shouldNotContain "Unexpected program argument"
    }

    @Test
    fun `Should log unexpected arguments`()
    {
        DartsClient.parseProgramArguments(arrayOf("foo"))

        getLogs() shouldContain "Unexpected program argument: foo"
    }

    @Test
    fun `Should leave the fields alone when no arguments passed`()
    {
        DartsClient.devMode = false
        DartsClient.justUpdated = false
        DartsClient.logSecret = ""

        DartsClient.parseProgramArguments(arrayOf())

        DartsClient.devMode shouldBe false
        DartsClient.justUpdated shouldBe false
        DartsClient.logSecret shouldBe ""
    }

    @Test
    fun `Should parse devMode argument`()
    {
        DartsClient.parseProgramArguments(arrayOf("devMode"))

        DartsClient.devMode shouldBe true
        getLogs() shouldContain "Running in dev mode"
    }

    @Test
    fun `Should parse justUpdated argument`()
    {
        DartsClient.parseProgramArguments(arrayOf("justUpdated"))

        DartsClient.justUpdated shouldBe true
        getLogs() shouldContain "I've just updated"
    }

    @Test
    fun `Should parse a value for the OS`()
    {
        DartsClient.operatingSystem.shouldNotBeEmpty()
    }

    @Test
    fun `Should report correctly whether on apple OS`()
    {
        val actualOs = DartsClient.operatingSystem

        DartsClient.operatingSystem = "windows"
        DartsClient.isAppleOs() shouldBe false

        DartsClient.operatingSystem = "darwin"
        DartsClient.isAppleOs() shouldBe true

        DartsClient.operatingSystem = "mac"
        DartsClient.isAppleOs() shouldBe true

        DartsClient.operatingSystem = actualOs
    }

    @Test
    fun `Should not bother checking for updates in dev mode`()
    {
        DartsClient.devMode = true

        val mock = mockk<UpdateManager>(relaxed = true)
        DartsClient.updateManager = mock

        DartsClient.checkForUpdatesIfRequired()

        getLogs() shouldContain "Not checking for updates as I'm in dev mode"
        verifyNotCalled { mock.checkForUpdates(any()) }
    }

    @Test
    fun `Should not bother checking for updates if just updated`()
    {
        DartsClient.devMode = false
        DartsClient.justUpdated = true

        val mock = mockk<UpdateManager>(relaxed = true)
        DartsClient.updateManager = mock

        DartsClient.checkForUpdatesIfRequired()

        getLogs() shouldContain "Just updated - not checking for updates"
        verifyNotCalled { mock.checkForUpdates(any()) }
    }

    @Test
    fun `Should not check for updates if preference is unset`()
    {
        DartsClient.devMode = false
        DartsClient.justUpdated = false
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, false)

        val mock = mockk<UpdateManager>(relaxed = true)
        DartsClient.updateManager = mock

        DartsClient.checkForUpdatesIfRequired()

        verifyNotCalled { mock.checkForUpdates(any()) }
    }


    @Test
    fun `Should check for updates if necessary`()
    {
        DartsClient.devMode = false
        DartsClient.justUpdated = false
        PreferenceUtil.saveBoolean(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES, true)

        val mock = mockk<UpdateManager>(relaxed = true)
        DartsClient.updateManager = mock

        DartsClient.checkForUpdatesIfRequired()

        verify { mock.checkForUpdates(DARTS_VERSION_NUMBER) }
    }
}