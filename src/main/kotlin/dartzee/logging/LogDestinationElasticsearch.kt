package dartzee.logging

import dartzee.db.BulkInserter
import dartzee.db.PendingLogsEntity
import dartzee.`object`.DartsClient
import dartzee.utils.InjectedThings.logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class LogDestinationElasticsearch(
    private val poster: ElasticsearchPoster?,
    private val scheduler: ScheduledExecutorService,
) : ILogDestination {
    private val pendingLogs = ConcurrentHashMap.newKeySet<String>()

    override fun log(record: LogRecord) {
        if (!DartsClient.devMode) {
            pendingLogs.add(record.toJsonString())
        }
    }

    override fun contextUpdated(context: Map<String, Any?>) {}

    fun startPosting() {
        val runnable = Runnable { postPendingLogs() }
        scheduler.scheduleAtFixedRate(runnable, 0, 10, TimeUnit.SECONDS)
    }

    fun postPendingLogs() {
        poster ?: return
        if (!poster.isOnline()) {
            return
        }

        val logsForThisRun = pendingLogs.toList()
        logsForThisRun.forEach(::postLogToElasticsearch)
    }

    private fun postLogToElasticsearch(logJson: String) {
        if (poster?.postLog(logJson) == true) {
            pendingLogs.remove(logJson)
        }
    }

    fun readOldLogs() {
        val persistedLogs = PendingLogsEntity().retrieveEntities().map { it.logJson }
        pendingLogs.addAll(persistedLogs)

        PendingLogsEntity().deleteAll()
    }

    fun shutDown() {
        try {
            scheduler.shutdown()
            logger.waitUntilLoggingFinished()

            val remainingLogs = pendingLogs.toList()
            val entities = remainingLogs.map { PendingLogsEntity.factory(it) }
            BulkInserter.insert(entities)
        } catch (e: Exception) {
            logger.error(CODE_ELASTICSEARCH_ERROR, "Failed to write out pending logs", e)
        }
    }
}
