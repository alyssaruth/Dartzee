package dartzee.logging

import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.AWSRequestSigningApacheInterceptor
import com.amazonaws.services.elasticsearch.AWSElasticsearch
import dartzee.utils.InjectedThings
import org.apache.http.HttpHost
import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import java.nio.charset.Charset
import java.util.*

private const val ELASTICSEARCH_URL = "https://search-dartzee-nfqeufkxsx6cu7sybhm53dts7e.eu-west-2.es.amazonaws.com"

class ElasticsearchPoster(private val credentials: AWSCredentials, private val indexPath: String)
{
    private val client = createClient()

    private fun createClient(): RestClient?
    {
        try
        {
            val signer = AWS4Signer().also {
                it.serviceName = AWSElasticsearch.ENDPOINT_PREFIX
                it.regionName = "eu-west-2"
            }

            val interceptor = AWSRequestSigningApacheInterceptor(signer.serviceName, signer, AWSStaticCredentialsProvider(credentials))
            return RestClient.builder(HttpHost.create(ELASTICSEARCH_URL))
                .setHttpClientConfigCallback { it.addInterceptorLast(interceptor) }.build()
        }
        catch (t: Throwable)
        {
            InjectedThings.logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to set up RestClient - won't post logs to ES", t)
            return null
        }
    }
    private fun readCredentials(): BasicAWSCredentials
    {
        val awsCredentials = javaClass.getResource("/aws").readText()
        val decoded = Base64.getDecoder().decode(awsCredentials).toString(Charset.forName("UTF-8"))
        val lines = decoded.lines()
        return BasicAWSCredentials(lines[0], lines[1])
    }

    fun postLog(logJson: String): Boolean
    {
        client ?: return false

        try
        {
            val request = Request("PUT", "/$indexPath/_doc/${UUID.randomUUID()}")
            request.entity = NStringEntity(logJson, ContentType.APPLICATION_JSON)

            val response = client.performRequest(request)
            val status = response.statusLine.statusCode
            if (status == HttpStatus.SC_CREATED)
            {
                return true
            }
            else
            {
                InjectedThings.logger.error(CODE_ELASTICSEARCH_ERROR, "Received status code $status trying to post to ES", Throwable(), KEY_RESPONSE_BODY to response)
                return false
            }
        }
        catch (t: Throwable)
        {
            InjectedThings.logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to post log to ES", t)
            return false
        }
    }
}