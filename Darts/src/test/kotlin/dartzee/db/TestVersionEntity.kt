package dartzee.db

import dartzee.db.VersionEntity
import dartzee.db.VersionEntity.Companion.insertVersion
import dartzee.utils.DartsDatabaseUtil
import dartzee.core.helper.exceptionLogged
import dartzee.helper.wipeTable
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
        exceptionLogged() shouldBe false
    }

    @Test
    fun `Should stack trace and return the first row if multiple versions found`()
    {
        wipeTable("Version")

        insertVersion()
        insertVersion()

        VersionEntity.retrieveCurrentDatabaseVersion()!!.version shouldBe DartsDatabaseUtil.DATABASE_VERSION
        exceptionLogged() shouldBe true
    }
}