package dartzee.db

import dartzee.db.sanity.getColumnsAllowingDefaults
import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.helper.insertParticipant
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV19toV20: AbstractTest()
{
    @Test
    fun `Should correctly add Team table, and TeamId column for Participant`()
    {
        // Setup
        val pt = insertParticipant()
        mainDatabase.dropTable(EntityName.Team)
        mainDatabase.executeUpdate("ALTER TABLE Participant DROP COLUMN TeamId")

        val conversions = DatabaseMigrations.getConversionsMap()[19]!!
        conversions.forEach { it(mainDatabase) }

        getCountFromTable(EntityName.Team) shouldBe 0
        getColumnsAllowingDefaults().shouldBeEmpty()

        val updatedPt = ParticipantEntity().retrieveForId(pt.rowId)!!
        updatedPt.teamId shouldBe ""
    }
}