package dartzee.db

import dartzee.dartzee.DartzeeRuleCalculationResult
import dartzee.helper.*
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV17toV18: AbstractTest()
{
    val oldString = """<CalculationResult AllCombinations="571787" AllCombinationsProbability="1.0000000000016696" ValidCombinationProbability="0.0031077077296269412" ValidCombinations="1728"><ScoringSegment Score="1" Type="DOUBLE"/><ScoringSegment Score="1" Type="TREBLE"/><ScoringSegment Score="1" Type="OUTER_SINGLE"/><ScoringSegment Score="1" Type="INNER_SINGLE"/><ScoringSegment Score="5" Type="DOUBLE"/><ScoringSegment Score="5" Type="TREBLE"/><ScoringSegment Score="5" Type="OUTER_SINGLE"/><ScoringSegment Score="5" Type="INNER_SINGLE"/><ScoringSegment Score="20" Type="DOUBLE"/><ScoringSegment Score="20" Type="TREBLE"/><ScoringSegment Score="20" Type="OUTER_SINGLE"/><ScoringSegment Score="20" Type="INNER_SINGLE"/><ValidSegment Score="1" Type="DOUBLE"/><ValidSegment Score="1" Type="TREBLE"/><ValidSegment Score="1" Type="OUTER_SINGLE"/><ValidSegment Score="1" Type="INNER_SINGLE"/><ValidSegment Score="5" Type="DOUBLE"/><ValidSegment Score="5" Type="TREBLE"/><ValidSegment Score="5" Type="OUTER_SINGLE"/><ValidSegment Score="5" Type="INNER_SINGLE"/><ValidSegment Score="20" Type="DOUBLE"/><ValidSegment Score="20" Type="TREBLE"/><ValidSegment Score="20" Type="OUTER_SINGLE"/><ValidSegment Score="20" Type="INNER_SINGLE"/></CalculationResult>"""

    @Test
    fun `should convert from old XML format to JSON`()
    {
        val oldResult = DartzeeRuleCalculationResult.fromDbStringOLD(oldString)

        val de = DartzeeRuleEntity()
        de.rowId = randomGuid()
        de.entityId = randomGuid()
        de.entityName = EntityName.Game
        de.calculationResult = oldString
        de.ordinal = 1
        de.saveToDatabase()

        DatabaseMigrations.convertDartzeeCalculationResults(mainDatabase)

        val resultJson = retrieveDartzeeRule().calculationResult
        val newResult = DartzeeRuleCalculationResult.fromDbString(resultJson)
        newResult shouldBe oldResult
    }
}