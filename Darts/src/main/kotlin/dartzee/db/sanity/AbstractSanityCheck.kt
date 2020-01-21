package dartzee.db.sanity

abstract class AbstractSanityCheck
{
    abstract fun runCheck(): List<AbstractSanityCheckResult>
}