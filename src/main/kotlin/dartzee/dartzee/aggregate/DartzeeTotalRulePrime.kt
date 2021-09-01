package dartzee.dartzee.aggregate

import org.apache.commons.math3.primes.Primes

class DartzeeTotalRulePrime: AbstractDartzeeTotalRule()
{
    override fun getRuleIdentifier() = "Prime"

    override fun isValidTotal(total: Int) = Primes.isPrime(total)
    override fun getDescription() = "Total is prime"
}