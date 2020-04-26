package dartzee.logging

import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.http.AWSRequestSigningApacheInterceptor
import com.amazonaws.services.elasticsearch.AWSElasticsearch
import org.apache.http.HttpHost
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
    val pendingLogs = ConcurrentHashMap.newKeySet<LogRecord>()

    private val scheduler = Executors.newScheduledThreadPool(1)
    private val client = createClient()

    init
    {
        val runnable = Runnable { postPendingLogs() }
        scheduler.scheduleAtFixedRate(runnable, 5, 5, TimeUnit.SECONDS)
    }

    override fun log(record: LogRecord)
    {
        pendingLogs.add(record)
    }

    override fun contextUpdated(context: Map<String, Any?>){}


    private fun createClient(): RestClient
    {
        val signer = AWS4Signer().also {
            it.serviceName = AWSElasticsearch.ENDPOINT_PREFIX
            it.regionName = "eu-west-2"
        }

        val credentials = readCredentials()
        val provider = AWSStaticCredentialsProvider(credentials)
        val interceptor = AWSRequestSigningApacheInterceptor(signer.serviceName, signer, provider)
        return RestClient.builder(HttpHost.create(ELASTICSEARCH_URL)).setHttpClientConfigCallback { it.addInterceptorLast(interceptor) }.build()
    }
    private fun readCredentials(): BasicAWSCredentials
    {
        val awsCredentials = javaClass.getResource("/aws").readText()
        val decoded = Base64.getDecoder().decode(awsCredentials).toString(Charset.forName("UTF-8"))
        val lines = decoded.lines()
        return BasicAWSCredentials(lines[0], lines[1])
    }

    fun postPendingLogs()
    {
        val logsForThisRun = pendingLogs.toList()
        logsForThisRun.forEach {
            val request = Request("PUT", "$INDEX_PATH/${UUID.randomUUID()}")
            request.entity = NStringEntity(it.toJsonString(), ContentType.APPLICATION_JSON)
            val response = client.performRequest(request)
            println(response)
        }
    }
}