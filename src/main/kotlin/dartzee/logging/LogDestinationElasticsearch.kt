package dartzee.logging

import com.amazonaws.auth.AWS4Signer
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.elasticsearch.AWSElasticsearch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val INDEX_PATH = "/dartzee/_doc/_create"

class LogDestinationElasticsearch: ILogDestination
{
    val pendingLogs = ConcurrentHashMap.newKeySet<LogRecord>()

    private val scheduler = Executors.newScheduledThreadPool(1)
    private val signer = factorySigner()

    init
    {
        val runnable = Runnable { postPendingLogs() }
        scheduler.scheduleAtFixedRate(runnable, 30, 30, TimeUnit.SECONDS)
    }

    override fun log(record: LogRecord)
    {
        pendingLogs.add(record)
    }

    override fun contextUpdated(context: Map<String, Any?>){}


    private fun createClient() {
        val signer = AWS4Signer().also {
            it.serviceName = AWSElasticsearch.ENDPOINT_PREFIX
            it.regionName = "eu-west-2"
        }

        val interceptor = AWSRequestSigningApacheInterceptor(signer.serviceName, signer, DefaultAWSCredentialsProviderChain())

    }
    fun postPendingLogs()
    {
        val logsForThisRun = pendingLogs.size

    }
}