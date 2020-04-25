package dartzee.helper

import dartzee.logging.ILogDestination
import dartzee.logging.LogRecord

class FakeLogDestination: ILogDestination
{
    val logRecords: MutableList<LogRecord> = mutableListOf()

    override fun log(record: LogRecord)
    {
        logRecords.add(record)
    }

    override fun contextUpdated(context: Map<String, Any?>) {}

    fun clear()
    {
        logRecords.clear()
    }
}