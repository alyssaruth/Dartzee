package dartzee.logging

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LogDestinationElasticsearch(private val poster: ElasticsearchPoster?): ILogDestination
{
    private val pendingLogs = ConcurrentHashMap.newKeySet<LogRecord>()

    private val scheduler = Executors.newScheduledThreadPool(1)

    override fun log(record: LogRecord)
    {
        pendingLogs.add(record)
    }

    override fun contextUpdated(context: Map<String, Any?>){}

    fun startPosting()
    {
        val runnable = Runnable { postPendingLogs() }
        scheduler.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS)
    }

    fun postPendingLogs()
    {
        val logsForThisRun = pendingLogs.toList()
        logsForThisRun.forEach(::postLogToElasticsearch)
    }
    private fun postLogToElasticsearch(log: LogRecord)
    {
        val logJson = log.toJsonString()
        if (poster?.postLog(logJson) == true)
        {
            pendingLogs.remove(log)
        }
    }
}