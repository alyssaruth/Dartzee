package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.wipeTable
import dartzee.utils.InjectedThings.database
import io.kotlintest.matchers.collections.shouldBeUnique
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Test

class TestLocalIdGenerator: AbstractTest()
{
    @Test
    fun `It should generate an ID of 1 for an empty table`()
    {
        wipeTable("Game")

        LocalIdGenerator.generateLocalId(database, "Game") shouldBe 1
    }

    @Test
    fun `It should generate the next ID for a non-empty table`()
    {
        wipeTable("Game")

        insertGame(localId = 5)

        LocalIdGenerator.generateLocalId(database, "Game") shouldBe 6
    }

    @Test
    fun `It should generate sequential IDs`()
    {
        LocalIdGenerator.hmLastAssignedIdByTableName["Test"] = 25

        val idOne = LocalIdGenerator.generateLocalId(database, "Test")
        val idTwo = LocalIdGenerator.generateLocalId(database, "Test")
        val idThree = LocalIdGenerator.generateLocalId(database, "Test")

        idOne shouldBe 26
        idTwo shouldBe 27
        idThree shouldBe 28
    }

    @Test
    fun `It should keep track of different entities`()
    {
        LocalIdGenerator.hmLastAssignedIdByTableName["foo"] = 5
        LocalIdGenerator.hmLastAssignedIdByTableName["bar"] = 25

        LocalIdGenerator.generateLocalId(database, "foo") shouldBe 6
        LocalIdGenerator.generateLocalId(database, "bar") shouldBe 26
    }

    @Test
    fun `It should be thread-safe`()
    {
        LocalIdGenerator.hmLastAssignedIdByTableName["foo"] = 0

        val threads = mutableListOf<Thread>()
        val runnables = mutableListOf<IdGeneratorRunnable>()
        for (i in 1..10)
        {
            val runnable = IdGeneratorRunnable(mutableListOf())
            runnables.add(runnable)

            threads.add(Thread(runnable))
        }

        threads.forEach{
            it.start()
        }

        threads.forEach{
            it.join()
        }

        val allIds = mutableListOf<Long>()
        runnables.forEach{
            allIds.addAll(it.list)
        }

        allIds.shouldHaveSize(200)
        allIds.shouldBeUnique()
    }

    private class IdGeneratorRunnable(val list: MutableList<Long>): Runnable
    {
        override fun run()
        {
            for (i in 1..20)
            {
                val id = LocalIdGenerator.generateLocalId(database, "foo")
                list.add(id)
            }
        }
    }
}