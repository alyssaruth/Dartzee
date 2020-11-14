package dartzee.sync

import dartzee.helper.AbstractTest

class TestSyncManager: AbstractTest()
{
//    @Test
//    fun `Should insert into SyncAudit and create remote database for the first time`()
//    {
//        val store = InMemoryRemoteDatabaseStore()
//        val manager = SyncManager(SyncMode.CREATE_REMOTE, "Goomba", store)
//        manager.doSync()
//
//        val result = store.fetchDatabase("Goomba")
//        result shouldBe mainDatabase
//        SyncAuditEntity.getLastSyncDate(result, "Goomba").shouldNotBeNull()
//        getRemoteName() shouldBe "Goomba"
//        dialogFactory.infosShown.shouldContainExactly("Sync completed successfully. Dartzee will now exit.")
//    }
//
//    @Test
//    fun `Should overwrite local database with remote, and insert a SyncAudit entry`()
//    {
//        usingInMemoryDatabase(filePath = "Remote", withSchema = true) { remoteDb ->
//            val f = File("Remote/Test.txt")
//            f.mkdirs()
//            f.createNewFile()
//
//            val store = InMemoryRemoteDatabaseStore()
//            store.pushDatabase("Goomba", remoteDb)
//
//            val manager = SyncManager(SyncMode.OVERWRITE_LOCAL, "Goomba", store)
//            manager.doSync()
//
//            val movedFile = File("$DATABASE_FILE_PATH/Test.txt")
//            movedFile.shouldExist()
//
//            getRemoteName() shouldBe "Goomba"
//            dialogFactory.infosShown.shouldContainExactly("Sync completed successfully. Dartzee will now exit.")
//        }
//    }
}