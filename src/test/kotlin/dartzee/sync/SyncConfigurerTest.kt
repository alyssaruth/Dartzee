package dartzee.sync

import dartzee.cancelOptionDialog
import dartzee.helper.AbstractTest
import dartzee.runAsync
import dartzee.selectFromOptionDialog
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SyncConfigurerTest : AbstractTest() {
    @Test
    fun `Should return return correct config when creating remote database for the first time`() {
        dialogFactory.inputSelection = "Goomba"

        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer().doFirstTimeSetup() }

        selectFromOptionDialog("Database not found", "Create 'Goomba'")

        result shouldBe SyncConfig(SyncMode.CREATE_REMOTE, "Goomba")
    }

    @Test
    fun `Should return null if prompt to create new remote database is cancelled`() {
        dialogFactory.inputSelection = "Goomba"

        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer().doFirstTimeSetup() }

        cancelOptionDialog("Database not found")
        result shouldBe null
    }

    @Test
    fun `Should return the right config when remote already exists and simple overwrite is chosen`() {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        dialogFactory.inputSelection = "Goomba"
        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer(store).doFirstTimeSetup() }

        selectFromOptionDialog("Database found", "Overwrite local data")

        result shouldBe SyncConfig(SyncMode.OVERWRITE_LOCAL, "Goomba")
    }

    @Test
    fun `Should return the right config when remote already exists and regular sync is chosen`() {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        dialogFactory.inputSelection = "Goomba"
        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer(store).doFirstTimeSetup() }

        selectFromOptionDialog("Database found", "Sync with local data")

        result shouldBe SyncConfig(SyncMode.NORMAL_SYNC, "Goomba")
    }

    @Test
    fun `Should return null when remote already exists and option is cancelled`() {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        dialogFactory.inputSelection = "Goomba"
        var result: SyncConfig? = null
        runAsync { result = makeSyncConfigurer(store).doFirstTimeSetup() }

        cancelOptionDialog("Database found")

        result shouldBe null
    }

    private fun makeSyncConfigurer(dbStore: IRemoteDatabaseStore = InMemoryRemoteDatabaseStore()) =
        SyncConfigurer(dbStore)
}
