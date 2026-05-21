package dartzee.sync

import dartzee.cancelOptionDialog
import dartzee.dismissDialog
import dartzee.helper.AbstractTest
import dartzee.runAsync
import dartzee.selectFromOptionDialog
import dartzee.typeIntoInputDialog
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SyncConfigurerTest : AbstractTest() {
    @Test
    fun `Should return return null if the input prompt for a name is cancelled`() {
        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer().doFirstTimeSetup() }

        dismissDialog("Sync Setup")

        result shouldBe null
    }

    @Test
    fun `Should return return correct config when creating remote database for the first time`() {
        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer().doFirstTimeSetup() }

        typeIntoInputDialog("Sync Setup", "Goomba")
        selectFromOptionDialog("Database not found", "Create 'Goomba'")

        result shouldBe SyncConfig(SyncMode.CREATE_REMOTE, "Goomba")
    }

    @Test
    fun `Should return null if prompt to create new remote database is cancelled`() {
        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer().doFirstTimeSetup() }

        typeIntoInputDialog("Sync Setup", "Goomba")
        cancelOptionDialog("Database not found")

        result shouldBe null
    }

    @Test
    fun `Should return the right config when remote already exists and simple overwrite is chosen`() {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer(store).doFirstTimeSetup() }

        typeIntoInputDialog("Sync Setup", "Goomba")
        selectFromOptionDialog("Database found", "Overwrite local data")

        result shouldBe SyncConfig(SyncMode.OVERWRITE_LOCAL, "Goomba")
    }

    @Test
    fun `Should return the right config when remote already exists and regular sync is chosen`() {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer(store).doFirstTimeSetup() }

        typeIntoInputDialog("Sync Setup", "Goomba")
        selectFromOptionDialog("Database found", "Sync with local data")

        result shouldBe SyncConfig(SyncMode.NORMAL_SYNC, "Goomba")
    }

    @Test
    fun `Should return null when remote already exists and option is cancelled`() {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer(store).doFirstTimeSetup() }

        typeIntoInputDialog("Sync Setup", "Goomba")
        cancelOptionDialog("Database found")

        result shouldBe null
    }

    private fun makeSyncConfigurer(dbStore: IRemoteDatabaseStore = InMemoryRemoteDatabaseStore()) =
        SyncConfigurer(dbStore)
}
