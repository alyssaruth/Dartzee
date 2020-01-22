package dartzee.db.sanity

import dartzee.db.sanity.SanityCheckColumnsThatAllowDefaults
import dartzee.utils.DatabaseUtil
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestSanityCheckSanityCheckColumnsThatAllowDefaults: AbstractTest()
{
    @Test
    fun `Should return no results by default`()
    {
        val check = SanityCheckColumnsThatAllowDefaults()
        val results = check.runCheck()

        results.shouldBeEmpty()
    }

    @Test
    fun `Should pick up on tables that allow defaults`()
    {
        val sql = "CREATE TABLE BadTable(Id INT PRIMARY KEY, OtherField VARCHAR(50) DEFAULT 'foo')"

        DatabaseUtil.executeUpdate(sql)

        val results = SanityCheckColumnsThatAllowDefaults().runCheck()

        val tm = results.first().getResultsModel()

        tm.getValueAt(0, 0) shouldBe "BADTABLE"
        tm.getValueAt(0, 1) shouldBe "OTHERFIELD"

        DatabaseUtil.dropTable("BadTable")
    }
}
