package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.getCountFromTable
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestDatabaseMigrationV18toV19: AbstractTest()
{
    @Test
    fun `Should correctly add DeletionAudit table`()
    {
        mainDatabase.dropTable(EntityName.DeletionAudit.name)

        DatabaseMigrations.createDeletionAudit(mainDatabase)

        getCountFromTable(EntityName.DeletionAudit) shouldBe 0
    }
}