package burlton.dartzee.test.db.sanity

import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.ParticipantEntity
import burlton.dartzee.code.db.sanity.SanityCheckUnsetIdFields
import burlton.dartzee.test.helper.AbstractTest
import burlton.dartzee.test.helper.insertGame
import burlton.dartzee.test.helper.insertParticipant
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.Test

class TestSanityCheckUnsetIdFields: AbstractTest()
{
    @Test
    fun `Should not flag up ID fields which are allowed to be unset`()
    {
        insertGame(dartsMatchId = "")

        val results = SanityCheckUnsetIdFields(GameEntity()).runCheck()
        results.shouldBeEmpty()
    }

    @Test
    fun `Should flag up unset ID fields`()
    {
        insertParticipant(gameId = "", playerId = "")

        val results = SanityCheckUnsetIdFields(ParticipantEntity()).runCheck()
        results.map { it.getDescription() }.shouldContainExactlyInAnyOrder("Participant rows where GameId is unset",
            "Participant rows where PlayerId is unset")
    }
}
