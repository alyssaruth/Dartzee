package dartzee.db

import dartzee.core.helper.verifyNotCalled
import dartzee.helper.*
import dartzee.utils.Database
import dartzee.utils.DatabaseMigrations
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.mockk.spyk
import org.junit.Test

class TestDatabaseMigrations: AbstractTest()
{
    override fun beforeEachTest()
    {
        InjectedThings.mainDatabase = Database(dbName = DATABASE_NAME_TEST)
        super.beforeEachTest()
    }

    @Test
    fun `V15 - V16 should create SyncAudit table`()
    {
        usingInMemoryDatabase(withSchema = true) { database ->
            database.dropTable("SyncAudit")
            database.updateDatabaseVersion(15)

            val migrator = DatabaseMigrator(DatabaseMigrations.getConversionsMap())
            migrator.migrateToLatest(database, "Test")

            database.getDatabaseVersion() shouldBe 16

            shouldNotThrowAny {
                SyncAuditEntity(database).retrieveForId("foo", false)
            }
        }
    }

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
        usingInMemoryDatabase(withSchema = true) { database ->
            val spiedDatabase = spyk(database)
            InjectedThings.mainDatabase = spiedDatabase

            usingInMemoryDatabase(withSchema = true) { dbToRunOn ->
                val conversionFns = DatabaseMigrations.getConversionsMap().values.flatten()
                for (conversion in conversionFns) {
                    println("Running conversion")
                    try {
                        conversion(dbToRunOn)
                    } catch (e: Exception) {
                    }
                }

                //Will probably have one logged, which is fine
                errorLogged()

                verifyNotCalled { spiedDatabase.borrowConnection() }
            }
        }
    }
}