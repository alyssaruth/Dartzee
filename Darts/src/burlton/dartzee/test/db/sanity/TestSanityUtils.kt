package burlton.dartzee.test.db.sanity

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.sanity.getIdColumns
import burlton.dartzee.test.helper.AbstractDartsTest
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test

class TestSanityUtils: AbstractDartsTest()
{
    @Test
    fun `Should only return the correct columns`()
    {
        getIdColumns(GameEntity()).shouldContainExactly("DartsMatchId")
    }
}
