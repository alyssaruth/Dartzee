package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.utils.DatabaseMigrations
import io.kotlintest.matchers.collections.shouldContainExactly
import org.junit.Test

class TestDatabaseMigrations: AbstractTest()
{
    @Test
    fun `Conversions map should not have gaps`()
    {
        val supportedVersions = DatabaseMigrations.getConversionsMap().keys
        val min = supportedVersions.min()!!
        val max = supportedVersions.max()!!

        supportedVersions.shouldContainExactly((min..max).toSet())
    }
}