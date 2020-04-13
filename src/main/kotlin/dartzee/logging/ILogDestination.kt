package dartzee.logging

interface ILogDestination
{
    fun log(record: LogRecord)
}