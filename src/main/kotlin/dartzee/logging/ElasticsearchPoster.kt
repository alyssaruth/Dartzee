package dartzee.logging

import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.http.AWSRequestSigningApacheInterceptor
import com.amazonaws.services.elasticsearch.AWSElasticsearch
import dartzee.utils.InjectedThings.logger
import org.apache.http.HttpHost
import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import java.io.InterruptedIOException
import java.net.SocketException
import java.util.*


class ElasticsearchPoster(private val credentials: AWSCredentials?,
                          private val url: String,
                          private val indexPath: String)
{
    private val client: RestClient? by lazy { createClient() }

    private fun createClient(): RestClient?
    {
        try
        {
            val signer = AWS4Signer().also {
                it.serviceName = AWSElasticsearch.ENDPOINT_PREFIX
                it.regionName = "eu-west-2"
            }

            val interceptor = AWSRequestSigningApacheInterceptor(signer.serviceName, signer, AWSStaticCredentialsProvider(credentials))
            return RestClient.builder(HttpHost.create(url))
                .setHttpClientConfigCallback { it.addInterceptorLast(interceptor) }
                .build()
        }
        catch (t: Throwable)
        {
            logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to set up RestClient - won't post logs to ES", t)
            return null
        }
    }

    fun postLog(logJson: String): Boolean
    {
        val initialisedClient = client
        initialisedClient ?: return false

        try
        {
            val request = Request("PUT", "/$indexPath/_doc/${UUID.randomUUID()}")
            request.entity = NStringEntity(logJson, ContentType.APPLICATION_JSON)

            val response = initialisedClient.performRequest(request)
            val status = response.statusLine.statusCode
            if (status == HttpStatus.SC_CREATED)
            {
                return true
            }
            else
            {
                logger.error(CODE_ELASTICSEARCH_ERROR, "Received status code $status trying to post to ES", Throwable(), KEY_RESPONSE_BODY to response)
                return false
            }
        }
        catch (t: Throwable)
        {
            when (t)
            {
                is SocketException, is InterruptedIOException -> logger.warn(CODE_ELASTICSEARCH_ERROR, "Caught $t trying to post to elasticsearch")
                else -> logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to post log to ES", t)
            }

            return false
        }
    }
}