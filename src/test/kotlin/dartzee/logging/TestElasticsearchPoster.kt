package dartzee.logging

import com.amazonaws.auth.BasicAWSCredentials
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import org.elasticsearch.client.ResponseException
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
        val poster = makePoster()
        poster.postLog("""{"message": "test"}""") shouldBe true
    }

    @Test
    fun `Should log an error if we fail to construct the RestClient, and not attempt to post any logs`()
    {
        val poster = makePoster(credentials = null)

        //lazy initialisation means we won't hit the error yet
        errorLogged() shouldBe false
        poster.postLog("foo")

        //We've attempted to post a log, so we'll hit the RestClient error now
        val log = verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.ERROR)
        log.message shouldBe "Failed to set up RestClient - won't post logs to ES"
        log.errorObject.shouldBeInstanceOf<IllegalArgumentException>()

        clearLogs()
        poster.postLog("foo")
        getLogRecords().shouldBeEmpty()
    }

    @Test
    fun `Should log an error when posting an individual log fails for something other than connection problems`()
    {
        val poster = makePoster(index = "denied")
        poster.postLog("""{"message": "test"}""") shouldBe false

        val log = verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.ERROR)
        log.message shouldBe "Failed to post log to ES"
        log.errorObject.shouldBeInstanceOf<ResponseException>()
        (log.errorObject as ResponseException).response.statusLine.statusCode shouldBe 403
    }

    @Test
    fun `Should just log a single warning line if posting a log flakes due to connection`()
    {
        val poster = makePoster(url = "172.16.0.0")
        poster.postLog("""{"message": "test"}""") shouldBe false
    }

    private fun makePoster(credentials: BasicAWSCredentials? = readCredentials(),
                           url: String = ELASTICSEARCH_URL,
                           index: String = "unittest"): ElasticsearchPoster
    {
        return ElasticsearchPoster(credentials, url, index)
    }

    private fun readCredentials(): BasicAWSCredentials? =
        try
        {
            val awsCredentials = javaClass.getResource("/aws").readText()
            val decoded = Base64.getDecoder().decode(awsCredentials).toString(Charset.forName("UTF-8"))
            val lines = decoded.lines()
            BasicAWSCredentials(lines[0], lines[1])
        }
        catch (t: Throwable)
        {
            null
        }
}