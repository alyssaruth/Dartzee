package burlton.dartzee.test.`object`

import burlton.core.test.helper.getLogs
import burlton.dartzee.code.`object`.DartsClient
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsClient: AbstractDartsTest()
{
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
}