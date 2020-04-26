package dartzee.logging

import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.AWSRequestSigningApacheInterceptor
import com.amazonaws.services.elasticsearch.AWSElasticsearch
import dartzee.utils.InjectedThings.logger
import org.apache.http.HttpHost
import org.apache.http.HttpStatus
import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClient
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val INDEX_PATH = "/dartzee/_doc"
private const val ELASTICSEARCH_URL = "https://search-dartzee-nfqeufkxsx6cu7sybhm53dts7e.eu-west-2.es.amazonaws.com"

class LogDestinationElasticsearch: ILogDestination
{
    private val pendingLogs = ConcurrentHashMap.newKeySet<LogRecord>()

    private val scheduler = Executors.newScheduledThreadPool(1)
    private val client = createClient()

    init
    {
        val runnable = Runnable { postPendingLogs() }
        scheduler.scheduleAtFixedRate(runnable, 5, 5, TimeUnit.SECONDS)
    }

    override fun log(record: LogRecord)
    {
        client?.let { pendingLogs.add(record) }
    }

    override fun contextUpdated(context: Map<String, Any?>){}


    private fun createClient(): RestClient?
    {
        try
        {
            val signer = AWS4Signer().also {
                it.serviceName = AWSElasticsearch.ENDPOINT_PREFIX
                it.regionName = "eu-west-2"
            }

            val credentials = readCredentials()
            val provider = AWSStaticCredentialsProvider(credentials)
            val interceptor = AWSRequestSigningApacheInterceptor(signer.serviceName, signer, provider)
            return RestClient.builder(HttpHost.create(ELASTICSEARCH_URL))
                .setHttpClientConfigCallback { it.addInterceptorLast(interceptor) }.build()
        }
        catch (t: Throwable)
        {
            logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to set up RestClient - won't post logs to ES", t)
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

    private fun postPendingLogs()
    {
        val logsForThisRun = pendingLogs.toList()
        logsForThisRun.forEach(::postLogToElasticsearch)
    }
    private fun postLogToElasticsearch(log: LogRecord)
    {
        client ?: return

        val logJson = log.toJsonString()

        try
        {
            val request = Request("PUT", "$INDEX_PATH/${UUID.randomUUID()}")
            request.entity = NStringEntity(logJson, ContentType.APPLICATION_JSON)

            val response = client.performRequest(request)
            val status = response.statusLine.statusCode
            if (status == HttpStatus.SC_CREATED)
            {
                pendingLogs.remove(log)
            }
            else
            {
                logger.error(CODE_ELASTICSEARCH_ERROR, "Received status code $status trying to post to ES", Throwable(), KEY_RESPONSE_BODY to response)
            }
        }
        catch (t: Throwable)
        {
            logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to post log to ES", t)
        }
    }
}