package dartzee.db

import dartzee.db.VersionEntity.Companion.insertVersion
import dartzee.helper.wipeTable
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.Severity
import dartzee.utils.DartsDatabaseUtil
import io.kotlintest.shouldBe
import org.junit.Test

class TestVersionEntity: AbstractEntityTest<VersionEntity>()
{
    override fun factoryDao() = VersionEntity()

    @Test
    fun `Should return null is there is no Version set`()
    {
        wipeTable("Version")

        VersionEntity.retrieveCurrentDatabaseVersion() shouldBe null
    }

    @Test
    fun `Should return the single DB version with no errors`()
    {
        wipeTable("Version")

        insertVersion()

        VersionEntity.retrieveCurrentDatabaseVersion()!!.version shouldBe DartsDatabaseUtil.DATABASE_VERSION
    }

    @Test
    fun `Should stack trace and return the first row if multiple versions found`()
    {
        wipeTable("Version")

        insertVersion()
        insertVersion()

        VersionEntity.retrieveCurrentDatabaseVersion()!!.version shouldBe DartsDatabaseUtil.DATABASE_VERSION
        val error = verifyLog(CODE_SQL_EXCEPTION, Severity.ERROR)
        error.message shouldBe "Retrieved 2 rows from Version. Expected 1. WhereSQL [1 = 1]"
    }
}