package dartzee.logging

interface ILogDestination
{
    fun log(record: LogRecord)
    fun contextUpdated(context: Map<String, Any?>)
}