package burlton.dartzee.test.db.sanity

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.sanity.getIdColumns
import burlton.dartzee.test.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test

class TestSanityUtils: AbstractTest()
{
    @Test
    fun `Should only return the correct columns`()
    {
        getIdColumns(GameEntity()).shouldContainExactly("DartsMatchId")
    }
}
