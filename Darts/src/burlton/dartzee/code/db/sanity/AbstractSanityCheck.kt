package burlton.dartzee.code.db.sanity

abstract class AbstractSanityCheck
{
    abstract fun runCheck(): List<AbstractSanityCheckResult>
}