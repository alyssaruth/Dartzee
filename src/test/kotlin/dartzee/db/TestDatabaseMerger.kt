package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.makeInMemoryDatabase
import dartzee.helper.makeInMemoryDatabaseWithSchema
import dartzee.logging.CODE_MERGE_ERROR
import dartzee.logging.CODE_TEST_CONNECTION_ERROR
import dartzee.logging.Severity
import dartzee.utils.DartsDatabaseUtil
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class TestDatabaseMerger: AbstractTest()
{
    @Test
    fun `Should return false if connecting to remote database fails`()
    {
        val remoteName = "jdbc:derby:memory:invalid;create=false"
        val remoteDatabase = Database("invalid", remoteName)

        val merger = makeDatabaseMerger(remoteDatabase = remoteDatabase)
        merger.validateMerge() shouldBe false
        verifyLog(CODE_TEST_CONNECTION_ERROR, Severity.ERROR)
        dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the remote database.")
    }

    @Test
    fun `Should return false and log an error if remote database version cannot be verified`()
    {
        val remoteDatabase = mockk<Database>(relaxed = true)
        every { remoteDatabase.testConnection() } returns true
        every { remoteDatabase.getDatabaseVersion() } returns null
        
        val merger = makeDatabaseMerger(remoteDatabase = remoteDatabase)
        merger.validateMerge() shouldBe false
        verifyLog(CODE_MERGE_ERROR, Severity.ERROR)
        dialogFactory.errorsShown.shouldContainExactly("An error occurred connecting to the remote database.")
    }

    @Test
    fun `Should return false if remote database has higher version`()
    {
        val remoteDatabase = makeInMemoryDatabase()
        remoteDatabase.updateDatabaseVersion(DartsDatabaseUtil.DATABASE_VERSION + 1)

        val merger = makeDatabaseMerger(remoteDatabase = remoteDatabase)
        merger.validateMerge() shouldBe false
        dialogFactory.errorsShown.shouldContainExactly("The remote database contains data written by a higher Dartzee version. \n\nYou will need to update to the latest version of Dartzee before syncing again.")
    }

    @Test
    fun `Should migrate remote database to latest version`()
    {
        val dbVersion = DartsDatabaseUtil.DATABASE_VERSION - 1
        val remoteDatabase = makeInMemoryDatabaseWithSchema()
        remoteDatabase.updateDatabaseVersion(dbVersion)

        val migrations = mapOf(dbVersion to listOf
            { database: Database -> database.executeUpdate("CREATE TABLE Test(RowId VARCHAR(36))") }
        )

        val migrator = DatabaseMigrator(migrations)
        val merger = makeDatabaseMerger(remoteDatabase = remoteDatabase, databaseMigrator = migrator)
        val result = merger.performMerge()!!

        result.getDatabaseVersion() shouldBe DartsDatabaseUtil.DATABASE_VERSION
        result.executeQueryAggregate("SELECT COUNT(1) FROM Test") shouldBe 0
    }

    @Test
    fun `Should insert into SyncAudit`()
    {
        val remoteDatabase = makeInMemoryDatabaseWithSchema()
        val merger = makeDatabaseMerger(remoteDatabase = remoteDatabase, remoteName = "Goomba")
        val result = merger.performMerge()!!

        SyncAuditEntity.getLastSyncDate(result, "Goomba").shouldNotBeNull()
    }

    private fun makeDatabaseMerger(localDatabase: Database = mainDatabase,
                                   remoteDatabase: Database = makeInMemoryDatabase(),
                                   databaseMigrator: DatabaseMigrator = DatabaseMigrator(emptyMap()),
                                   remoteName: String = "Goomba") =
        DatabaseMerger(localDatabase, remoteDatabase, databaseMigrator, remoteName)
}