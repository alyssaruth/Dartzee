package dartzee.logging

class LogDestinationSystemOut: ILogDestination
{
    override fun log(record: LogRecord)
    {
        println(record)
        record.getThrowableStr()?.let { println(it) }
        record.keyValuePairs[KEY_STACK]?.let { println(it) }
    }
}