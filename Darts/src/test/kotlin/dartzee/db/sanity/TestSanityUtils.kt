package dartzee.test.db.sanity

import dartzee.db.GameEntity
import dartzee.db.sanity.getIdColumns
import dartzee.test.helper.AbstractTest
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
