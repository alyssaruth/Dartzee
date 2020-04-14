package dartzee.helper

import dartzee.logging.ILogDestination
import dartzee.logging.LogRecord
import dartzee.logging.Logger

class FakeLogDestination: ILogDestination
{
    val logRecords: MutableList<LogRecord> = mutableListOf()

    override fun log(record: LogRecord)
    {
        logRecords.add(record)
    }

    fun awaitLogs(): List<LogRecord>
    {
        Logger.waitUntilLoggingFinished()
        return logRecords.toList()
    }

    fun clear()
    {
        logRecords.clear()
    }
}