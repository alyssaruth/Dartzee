package dartzee.db

import dartzee.game.MatchMode
import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV16toV17: AbstractTest()
{
    @Test
    fun `Should correctly convert old XML to JSON`()
    {
        val xmlParams = """<MatchParams Fifth="1" First="5" Fourth="2" Second="4" Sixth="0" Third="3"/>"""
        val match = insertDartsMatch(mode = MatchMode.POINTS , matchParams = xmlParams)

        DatabaseMigrations.convertMatchParams(mainDatabase)

        val updatedMatch = DartsMatchEntity().retrieveForId(match.rowId)!!
        updatedMatch.matchParams shouldBe """{"1":5,"2":4,"3":3,"4":2,"5":1,"6":0}"""

        updatedMatch.getScoreForFinishingPosition(1) shouldBe 5
        updatedMatch.getScoreForFinishingPosition(2) shouldBe 4
        updatedMatch.getScoreForFinishingPosition(3) shouldBe 3
        updatedMatch.getScoreForFinishingPosition(4) shouldBe 2
        updatedMatch.getScoreForFinishingPosition(5) shouldBe 1
        updatedMatch.getScoreForFinishingPosition(6) shouldBe 0
    }

    @Test
    fun `Should leave FIRST_TO matches alone`()
    {
        val match = insertDartsMatch(mode = MatchMode.POINTS , matchParams = "")

        DatabaseMigrations.convertMatchParams(mainDatabase)

        val updatedMatch = DartsMatchEntity().retrieveForId(match.rowId)!!
        updatedMatch.matchParams shouldBe ""
    }
}