package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.insertGame
import dartzee.helper.wipeTable
import dartzee.utils.InjectedThings.mainDatabase
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

        LocalIdGenerator(mainDatabase).generateLocalId("Game") shouldBe 1
    }

    @Test
    fun `It should generate the next ID for a non-empty table`()
    {
        wipeTable("Game")

        insertGame(localId = 5)

        LocalIdGenerator(mainDatabase).generateLocalId("Game") shouldBe 6
    }

    @Test
    fun `It should generate sequential IDs`()
    {
        val generator = LocalIdGenerator(mainDatabase)
        generator.hmLastAssignedIdByTableName["Test"] = 25

        val idOne = generator.generateLocalId("Test")
        val idTwo = generator.generateLocalId("Test")
        val idThree = generator.generateLocalId("Test")

        idOne shouldBe 26
        idTwo shouldBe 27
        idThree shouldBe 28
    }

    @Test
    fun `It should keep track of different entities`()
    {
        val generator = LocalIdGenerator(mainDatabase)
        generator.hmLastAssignedIdByTableName["foo"] = 5
        generator.hmLastAssignedIdByTableName["bar"] = 25

        generator.generateLocalId("foo") shouldBe 6
        generator.generateLocalId("bar") shouldBe 26
    }

    @Test
    fun `It should be thread-safe`()
    {
        val generator = LocalIdGenerator(mainDatabase)
        generator.hmLastAssignedIdByTableName["foo"] = 0

        val threads = mutableListOf<Thread>()
        val runnables = mutableListOf<IdGeneratorRunnable>()
        for (i in 1..10)
        {
            val runnable = IdGeneratorRunnable(generator, mutableListOf())
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

    private class IdGeneratorRunnable(val generator: LocalIdGenerator, val list: MutableList<Long>): Runnable
    {
        override fun run()
        {
            for (i in 1..20)
            {
                val id = generator.generateLocalId("foo")
                list.add(id)
            }
        }
    }
}