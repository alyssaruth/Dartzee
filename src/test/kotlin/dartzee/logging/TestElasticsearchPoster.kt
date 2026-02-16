package dartzee.logging

import com.amazonaws.auth.BasicAWSCredentials
import dartzee.helper.AbstractTest
import dartzee.utils.AwsUtils
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.apache.http.HttpVersion
import org.apache.http.message.BasicStatusLine
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseException
import org.elasticsearch.client.RestClient
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TestElasticsearchPoster : AbstractTest() {
    @Test
    @Tag("integration")
    fun `Should report as online`() {
        Assumptions.assumeTrue { AwsUtils.readCredentials("AWS_LOGS") != null }

        val poster = makePoster()
        poster.isOnline() shouldBe true
    }

    @Test
    @Tag("integration")
    fun `Should post a test log successfully`() {
        Assumptions.assumeTrue { AwsUtils.readCredentials("AWS_LOGS") != null }

        val poster = makePoster()
        poster.postLog("""{"message": "test"}""") shouldBe true
    }

    @Test
    @Tag("integration")
    fun `Should log an error when posting an individual log fails for something other than connection problems`() {
        Assumptions.assumeTrue { AwsUtils.readCredentials("AWS_LOGS") != null }

        val poster = makePoster(index = "denied")
        poster.postLog("""{"message": "test"}""") shouldBe false

        val log = verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.ERROR)
        log.message shouldBe "Received status code 403 trying to post to ES"
        val errorObject = log.errorObject
        errorObject.shouldBeInstanceOf<ResponseException>()
        errorObject.response.statusLine.statusCode shouldBe 403
    }

    @Test
    @Tag("integration")
    fun `Should report as offline and not log if there is a connection error`() {
        Assumptions.assumeTrue { AwsUtils.readCredentials("AWS_LOGS") != null }

        val poster = makePoster(url = "172.16.0.0")
        poster.isOnline() shouldBe false

        verifyNoLogs(CODE_ELASTICSEARCH_ERROR)
    }

    @Test
    @Tag("integration")
    fun `Should just log a single warning line if posting a log flakes due to connection`() {
        Assumptions.assumeTrue { AwsUtils.readCredentials("AWS_LOGS") != null }

        val poster = makePoster(url = "172.16.0.0")
        poster.postLog("""{"message": "test"}""") shouldBe false

        verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.WARN)
    }

    @Test
    fun `Should report as offline if we fail to construct the RestClient`() {
        val poster = makePoster(credentials = null)
        poster.isOnline() shouldBe false
        clearLogs() // We'll get an error the first time due to initialising the RestClient

        poster.isOnline() shouldBe false
        errorLogged() shouldBe false
    }

    @Test
    fun `Should log an error if we fail to construct the RestClient, and not attempt to post any logs`() {
        val poster = makePoster(credentials = null)

        // lazy initialisation means we won't hit the error yet
        errorLogged() shouldBe false
        poster.postLog("foo")

        // We've attempted to post a log, so we'll hit the RestClient error now
        val log = verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.ERROR)
        log.message shouldBe "Failed to set up RestClient - won't post logs to ES"
        log.errorObject.shouldBeInstanceOf<IllegalArgumentException>()

        clearLogs()
        poster.postLog("foo")
        flushAndGetLogRecords().shouldBeEmpty()
    }

    @Test
    fun `Should report as offline if we get an unexpected status code`() {
        val client = mockk<RestClient>(relaxed = true)
        val response = makeResponse(503)
        every { client.performRequest(any()) } returns response

        val poster = makePoster(client = client)
        poster.isOnline() shouldBe false
    }

    @Test
    fun `Should report as offline and log an error if an unexpected exception is thrown`() {
        val exception = Throwable("Argh")
        val client = mockk<RestClient>(relaxed = true)
        every { client.performRequest(any()) } throws exception

        val poster = makePoster(client = client)
        poster.isOnline() shouldBe false

        val log = verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.ERROR)
        log.message shouldBe "Unexpected error checking if we are online"
        log.errorObject shouldBe exception
    }

    @Test
    fun `Should log an error if we get an unexpected response code (but no error) from ES`() {
        val client = mockk<RestClient>(relaxed = true)
        val response = makeResponse(409)
        every { client.performRequest(any()) } returns response

        val poster = makePoster(client = client)
        poster.postLog("""{"message": "test"}""") shouldBe false

        val log = verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.ERROR)
        log.errorObject.shouldBeInstanceOf<ResponseException>()
        log.keyValuePairs[KEY_RESPONSE_BODY] shouldBe response
        log.message shouldBe "Received status code 409 trying to post to ES"
    }

    @Test
    fun `Should handle 503 ResponseExceptions thrown by the client`() {
        val client = mockk<RestClient>(relaxed = true)
        val response = makeResponse(503)
        every { client.performRequest(any()) } throws ResponseException(response)

        val poster = makePoster(client = client)
        poster.postLog("""{"message": "test"}""") shouldBe false

        val log = verifyLog(CODE_ELASTICSEARCH_ERROR, Severity.WARN)
        log.keyValuePairs[KEY_RESPONSE_BODY] shouldBe response
        log.message shouldBe "Elasticsearch currently unavailable - got 503 response"
    }

    private fun makeResponse(statusCode: Int): Response {
        val statusLine = BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, "foo")
        val response = mockk<Response>(relaxed = true)
        every { response.statusLine } returns statusLine
        return response
    }

    private fun makePoster(
        credentials: BasicAWSCredentials? = AwsUtils.readCredentials("AWS_LOGS"),
        url: String = ELASTICSEARCH_URL,
        index: String = "unittest",
        client: RestClient? = null,
    ) = ElasticsearchPoster(credentials, url, index, client)
}
