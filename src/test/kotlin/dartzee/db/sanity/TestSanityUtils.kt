package dartzee.db.sanity

import dartzee.db.GameEntity
import dartzee.helper.AbstractTest
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class TestSanityUtils: AbstractTest()
{
    @Test
    fun `Should only return the correct columns`()
    {
        getIdColumns(GameEntity()).shouldContainExactly("DartsMatchId")
    }
}
