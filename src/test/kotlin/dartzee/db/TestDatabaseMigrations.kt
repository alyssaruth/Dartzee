package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.usingInMemoryDatabase
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

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

    @Test
    fun `Conversions should all run on the specified database`()
    {
        try
        {
            usingInMemoryDatabase(withSchema = true) { dbToRunOn ->
                mainDatabase.shutDown() shouldBe true

                val conversionFns = DatabaseMigrations.getConversionsMap().values.flatten()
                for (conversion in conversionFns)
                {
                    try { conversion(dbToRunOn) } catch (e: Exception) {}
                }

                //Will probably have one logged, which is fine
                errorLogged()

                //If it's been connected to during the test, then another shut down would succeed
                mainDatabase.shutDown() shouldBe false
            }
        }
        finally
        {
            mainDatabase.initialiseConnectionPool(1)
        }
    }
}