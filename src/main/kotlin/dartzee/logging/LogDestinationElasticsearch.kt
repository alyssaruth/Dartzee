package dartzee.logging

import dartzee.db.BulkInserter
import dartzee.db.PendingLogsEntity
import dartzee.utils.InjectedThings.logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class LogDestinationElasticsearch(private val poster: ElasticsearchPoster?, private val scheduler: ScheduledExecutorService): ILogDestination
{
    private val pendingLogs = ConcurrentHashMap.newKeySet<String>()

    override fun log(record: LogRecord)
    {
        pendingLogs.add(record.toJsonString())
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
    private fun postLogToElasticsearch(logJson: String)
    {
        if (poster?.postLog(logJson) == true)
        {
            pendingLogs.remove(logJson)
        }
    }


    fun readOldLogs()
    {
        val persistedLogs = PendingLogsEntity().retrieveEntities().map { it.logJson }
        pendingLogs.addAll(persistedLogs)

        PendingLogsEntity().deleteAll()
    }

    fun shutDown()
    {
        scheduler.shutdown()
        logger.waitUntilLoggingFinished()

        val remainingLogs = pendingLogs.toList()
        val entities = remainingLogs.map { PendingLogsEntity.factory(it) }
        BulkInserter.insert(entities)
    }

}