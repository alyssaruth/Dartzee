package dartzee.db.sanity

import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSanityCheckSanityCheckColumnsThatAllowDefaults : AbstractTest() {
    @Test
    fun `Should return no results by default`() {
        val check = SanityCheckColumnsThatAllowDefaults()
        val results = check.runCheck()

        results.shouldBeEmpty()
    }

    @Test
    fun `Should pick up on tables that allow defaults`() {
        val sql = "CREATE TABLE BadTable(Id INT PRIMARY KEY, OtherField VARCHAR(50) DEFAULT 'foo')"

        mainDatabase.executeUpdate(sql)

        val results = SanityCheckColumnsThatAllowDefaults().runCheck()

        val tm = results.first().getResultsModel()

        tm.getValueAt(0, 0) shouldBe "BADTABLE"
        tm.getValueAt(0, 1) shouldBe "OTHERFIELD"

        mainDatabase.dropTable("BadTable")
    }
}
