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

    fun clear()
    {
        logRecords.clear()
    }
}