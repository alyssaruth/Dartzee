package dartzee.logging

import java.util.concurrent.ConcurrentHashMap

class LogDestinationElasticsearch: ILogDestination
{
    val pendingLogs = ConcurrentHashMap.newKeySet<LogRecord>()

    override fun log(record: LogRecord)
    {
        pendingLogs.add(record)
    }

    override fun contextUpdated(context: Map<String, Any?>){}
}