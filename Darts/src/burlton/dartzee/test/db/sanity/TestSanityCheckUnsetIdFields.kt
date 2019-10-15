package burlton.dartzee.test.db.sanity

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.sanity.SanityCheckUnsetIdFields
import burlton.dartzee.test.helper.AbstractDartsTest
import burlton.dartzee.test.helper.insertGame
import io.kotlintest.matchers.collections.shouldBeEmpty
import org.junit.Test

class TestSanityCheckUnsetIdFields: AbstractDartsTest()
{
    @Test
    fun `Should not flag up ID fields which are allowed to be unset`()
    {
        insertGame(dartsMatchId = "")

        val results = SanityCheckUnsetIdFields(GameEntity()).runCheck()
        results.shouldBeEmpty()
    }
}
