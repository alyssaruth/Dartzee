package dartzee.db.sanity

import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.insertParticipant
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
