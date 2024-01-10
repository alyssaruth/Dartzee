package dartzee.helper

import dartzee.logging.CODE_SQL
import dartzee.logging.ILogDestination
import dartzee.logging.LogRecord

class FakeLogDestination : ILogDestination {
    val logRecords: MutableList<LogRecord> = mutableListOf()
    var haveRunInsert = false

    override fun log(record: LogRecord) {
        logRecords.add(record)

        if (record.loggingCode == CODE_SQL && record.message.contains("INSERT")) {
            haveRunInsert = true
        }
    }

    override fun contextUpdated(context: Map<String, Any?>) {}

    fun clear() {
        logRecords.clear()
    }
}
