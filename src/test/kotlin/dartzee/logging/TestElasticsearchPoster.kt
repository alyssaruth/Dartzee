package dartzee.logging

import com.amazonaws.auth.BasicAWSCredentials
import dartzee.helper.AbstractTest
import io.kotlintest.shouldBe
import org.junit.Assume
import org.junit.Test
import java.nio.charset.Charset
import java.util.*

class TestElasticsearchPoster: AbstractTest()
{
    override fun beforeEachTest()
    {
        Assume.assumeNotNull(readCredentials())
        super.beforeEachTest()
    }

    @Test
    fun `Should post a test log successfully`()
    {
        val poster = ElasticsearchPoster(readCredentials()!!, "unittest")

        poster.postLog("""{"message": "test"}""") shouldBe true
    }

    private fun readCredentials(): BasicAWSCredentials?
    {
        try
        {
            val awsCredentials = javaClass.getResource("/aws").readText()
            val decoded = Base64.getDecoder().decode(awsCredentials).toString(Charset.forName("UTF-8"))
            val lines = decoded.lines()
            return BasicAWSCredentials(lines[0], lines[1])
        }
        catch (t: Throwable)
        {
            return null
        }
    }
}