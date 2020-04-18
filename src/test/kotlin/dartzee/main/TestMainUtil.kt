package dartzee.main

import dartzee.`object`.DartsClient
import dartzee.helper.AbstractTest
import dartzee.logging.CODE_LOOK_AND_FEEL_ERROR
import dartzee.logging.CODE_LOOK_AND_FEEL_SET
import dartzee.logging.Severity
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.junit.Test
import javax.swing.UIManager
import javax.swing.plaf.metal.MetalLookAndFeel
import javax.swing.plaf.nimbus.NimbusLookAndFeel

class TestMainUtil: AbstractTest()
{
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
}