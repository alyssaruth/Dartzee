package dartzee.db.sanity

interface ISanityCheck {
    fun runCheck(): List<AbstractSanityCheckResult>
}
