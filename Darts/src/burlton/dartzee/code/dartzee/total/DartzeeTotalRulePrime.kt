package burlton.dartzee.code.dartzee.total

import burlton.dartzee.code.dartzee.AbstractDartzeeRule
import org.apache.commons.math3.primes.Primes

class DartzeeTotalRulePrime: AbstractDartzeeRule(), IDartzeeTotalRule
{
    override fun getRuleIdentifier() = "Prime"

    override fun isValidTotal(total: Int) = Primes.isPrime(total)
}