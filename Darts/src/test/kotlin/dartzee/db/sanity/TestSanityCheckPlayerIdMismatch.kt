package dartzee.test.db.sanity

import dartzee.db.sanity.SanityCheckPlayerIdMismatch
import dartzee.test.helper.AbstractTest
import dartzee.test.helper.insertDart
import dartzee.test.helper.insertParticipant
import dartzee.test.helper.randomGuid
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestSanityCheckPlayerIdMismatch: AbstractTest()
{
    @Test
    fun `Should not flag up matching rows`()
    {
        val pt = insertParticipant()
        insertDart(pt)

        val results = SanityCheckPlayerIdMismatch().runCheck()
        results.shouldBeEmpty()
    }

    @Test
    fun `Should flag up mismatching rows`()
    {
        val pt = insertParticipant()

        val ptPlayerId = pt.playerId
        val newPlayerId = randomGuid()
        pt.playerId = newPlayerId

        val drt = insertDart(pt)

        val results = SanityCheckPlayerIdMismatch().runCheck()
        val tm = results.first().getResultsModel()

        tm.getValueAt(0, 0) shouldBe drt.rowId
        tm.getValueAt(0, 1) shouldBe pt.rowId
        tm.getValueAt(0, 2) shouldBe newPlayerId
        tm.getValueAt(0, 3) shouldBe ptPlayerId
    }
}