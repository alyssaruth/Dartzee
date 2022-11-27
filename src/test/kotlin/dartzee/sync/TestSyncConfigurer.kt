package dartzee.sync

import dartzee.helper.AbstractTest
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestSyncConfigurer: AbstractTest()
{
    @Test
    fun `Should return return correct config when creating remote database for the first time`()
    {
        dialogFactory.inputSelection = "Goomba"
        dialogFactory.optionSequence.add("Create 'Goomba'")

        val result = makeSyncConfigurer().doFirstTimeSetup()
        dialogFactory.optionsShown.shouldContainExactly("No shared database found called 'Goomba'. Would you like to create it?")
        result shouldBe SyncConfig(SyncMode.CREATE_REMOTE, "Goomba")
    }

    @Test
    fun `Should return null if prompt to create new remote database is cancelled`()
    {
        dialogFactory.inputSelection = "Goomba"
        dialogFactory.optionSequence.add("Cancel")

        val result = makeSyncConfigurer().doFirstTimeSetup()
        dialogFactory.optionsShown.shouldContainExactly("No shared database found called 'Goomba'. Would you like to create it?")
        result shouldBe null
    }

    @Test
    fun `Should return null if prompt to create new remote database is escaped`()
    {
        dialogFactory.inputSelection = "Goomba"
        dialogFactory.optionSequence.add(null)

        val result = makeSyncConfigurer().doFirstTimeSetup()
        dialogFactory.optionsShown.shouldContainExactly("No shared database found called 'Goomba'. Would you like to create it?")
        result shouldBe null
    }

    @Test
    fun `Should return the right config when remote already exists and simple overwrite is chosen`()
    {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        dialogFactory.inputSelection = "Goomba"
        dialogFactory.optionSequence.add("Overwrite local data")

        val configurer = makeSyncConfigurer(store)
        val result = configurer.doFirstTimeSetup()
        dialogFactory.optionsShown.shouldContainExactly("Shared database 'Goomba' already exists. How would you like to proceed?")
        result shouldBe SyncConfig(SyncMode.OVERWRITE_LOCAL, "Goomba")
    }

    @Test
    fun `Should return the right config when remote already exists and regular sync is chosen`()
    {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        dialogFactory.inputSelection = "Goomba"
        dialogFactory.optionSequence.add("Sync with local data")

        val configurer = makeSyncConfigurer(store)
        val result = configurer.doFirstTimeSetup()
        dialogFactory.optionsShown.shouldContainExactly("Shared database 'Goomba' already exists. How would you like to proceed?")
        result shouldBe SyncConfig(SyncMode.NORMAL_SYNC, "Goomba")
    }

    @Test
    fun `Should return null when remote already exists and option is cancelled`()
    {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        dialogFactory.inputSelection = "Goomba"
        dialogFactory.optionSequence.add("Cancel")

        val configurer = makeSyncConfigurer(store)
        val result = configurer.doFirstTimeSetup()
        dialogFactory.optionsShown.shouldContainExactly("Shared database 'Goomba' already exists. How would you like to proceed?")
        result shouldBe null
    }

    @Test
    fun `Should return null when remote already exists and option is escaped`()
    {
        val store = InMemoryRemoteDatabaseStore()
        store.pushDatabase("Goomba", mainDatabase)

        dialogFactory.inputSelection = "Goomba"
        dialogFactory.optionSequence.add(null)

        val configurer = makeSyncConfigurer(store)
        val result = configurer.doFirstTimeSetup()
        dialogFactory.optionsShown.shouldContainExactly("Shared database 'Goomba' already exists. How would you like to proceed?")
        result shouldBe null
    }

    private fun makeSyncConfigurer(dbStore: IRemoteDatabaseStore = InMemoryRemoteDatabaseStore())
            = SyncConfigurer(dbStore)
}