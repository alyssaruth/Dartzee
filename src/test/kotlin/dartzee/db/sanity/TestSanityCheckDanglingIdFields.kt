package dartzee.db.sanity

import dartzee.db.DartzeeRuleEntity
import dartzee.db.GameEntity
import dartzee.db.EntityName
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertDartzeeRule
import dartzee.helper.insertGame
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestSanityCheckDanglingIdFields: AbstractTest()
{
    @Test
    fun `Should flag up ID fields that point at non-existent rows`()
    {
        val gameId = insertGame(dartsMatchId = "foo").rowId

        val results = SanityCheckDanglingIdFields(GameEntity()).runCheck()
        results.size shouldBe 1

        val result = results.first() as SanityCheckResultDanglingIdFields
        result.entities.first().rowId shouldBe gameId
        result.getDescription() shouldBe "Game rows where the DartsMatchId points at a non-existent DartsMatch"
    }

    @Test
    fun `Should not flag up an ID field that points at a row that exists`()
    {
        val matchId = insertDartsMatch().rowId
        insertGame(dartsMatchId = matchId)

        val results = SanityCheckDanglingIdFields(GameEntity()).runCheck()
        results.shouldBeEmpty()
    }

    @Test
    fun `Should not flag up an ID field which is empty`()
    {
        insertGame(dartsMatchId = "")

        val results = SanityCheckDanglingIdFields(GameEntity()).runCheck()
        results.shouldBeEmpty()
    }

    @Test
    fun `Should flag up generic EntityId+EntityName pairs`()
    {
        insertDartzeeRule(entityName = EntityName.Game, entityId = "Foo")
        insertDartzeeRule(entityName = EntityName.Game, entityId = "Bar")
        insertDartzeeRule(entityName = EntityName.Player, entityId = "Baz")

        val results = SanityCheckDanglingIdFields(DartzeeRuleEntity()).runCheck()
        results.size shouldBe 2

        results.map { it.getDescription() }.shouldContainExactlyInAnyOrder("DartzeeRule rows where the EntityId points at a non-existent Game",
            "DartzeeRule rows where the EntityId points at a non-existent Player")
    }
}