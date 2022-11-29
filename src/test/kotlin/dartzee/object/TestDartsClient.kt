package dartzee.`object`

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.AbstractRegistryTest
import dartzee.logging.CODE_JUST_UPDATED
import dartzee.logging.CODE_UNEXPECTED_ARGUMENT
import dartzee.logging.CODE_UPDATE_CHECK
import dartzee.logging.Severity
import dartzee.utils.DARTS_VERSION_NUMBER
import dartzee.utils.PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES
import dartzee.utils.PreferenceUtil
import dartzee.utils.UpdateManager
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestDartsClient: AbstractRegistryTest()
{
    override fun getPreferencesAffected() = listOf(PREFERENCES_BOOLEAN_CHECK_FOR_UPDATES)

    @Test
    fun `Should log unexpected arguments`()
    {
        DartsClient.parseProgramArguments(arrayOf("foo"))

        val log = verifyLog(CODE_UNEXPECTED_ARGUMENT, Severity.WARN)
        log.message shouldContain "foo"
    }

    @Test
    fun `Should leave the fields alone when no arguments passed`()
    {
        DartsClient.devMode = false
        DartsClient.justUpdated = false

        DartsClient.parseProgramArguments(arrayOf())

        DartsClient.devMode shouldBe false
        DartsClient.justUpdated shouldBe false
    }

    @Test
    fun `Should parse devMode argument`()
    {
        DartsClient.parseProgramArguments(arrayOf("devMode"))

        DartsClient.devMode shouldBe true
    }

    @Test
    fun `Should parse justUpdated argument`()
    {
        DartsClient.parseProgramArguments(arrayOf("justUpdated"))
        DartsClient.logArgumentState()

        DartsClient.justUpdated shouldBe true
        verifyLog(CODE_JUST_UPDATED)
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

        val log = verifyLog(CODE_UPDATE_CHECK)
        log.message shouldBe "Not checking for updates: I'm in dev mode"
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

        val log = verifyLog(CODE_UPDATE_CHECK)
        log.message shouldBe "Not checking for updates: just updated"
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

        val log = verifyLog(CODE_UPDATE_CHECK)
        log.message shouldBe "Not checking for updates: preference disabled"
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