package burlton.dartzee.test.db

import burlton.dartzee.code.db.LocalIdGenerator
import io.kotlintest.matchers.collections.shouldBeUnique
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import org.junit.Before
import org.junit.Test

class TestLocalIdGenerator
{
    @Before
    fun setup()
    {
        LocalIdGenerator.hmLastAssignedIdByTableName.clear()
    }

    @Test
    fun `It should generate sequential IDs`()
    {
        LocalIdGenerator.hmLastAssignedIdByTableName["Test"] = 25

        val idOne = LocalIdGenerator.generateLocalId("Test")
        val idTwo = LocalIdGenerator.generateLocalId("Test")
        val idThree = LocalIdGenerator.generateLocalId("Test")

        idOne shouldBe 26
        idTwo shouldBe 27
        idThree shouldBe 28
    }

    @Test
    fun `It should keep track of different entities`()
    {
        LocalIdGenerator.hmLastAssignedIdByTableName["foo"] = 5
        LocalIdGenerator.hmLastAssignedIdByTableName["bar"] = 25

        LocalIdGenerator.generateLocalId("foo") shouldBe 6
        LocalIdGenerator.generateLocalId("bar") shouldBe 26
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
                val id = LocalIdGenerator.generateLocalId("foo")
                list.add(id)
            }
        }
    }
}