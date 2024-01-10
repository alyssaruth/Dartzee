package dartzee.db

import dartzee.helper.AbstractTest
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertGame
import dartzee.utils.InjectedThings.mainDatabase
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestLocalIdGenerator : AbstractTest() {
    @Test
    fun `It should generate an ID of 1 for an empty table`() {
        LocalIdGenerator(mainDatabase).generateLocalId(EntityName.Game) shouldBe 1
    }

    @Test
    fun `It should generate the next ID for a non-empty table`() {
        insertGame(localId = 5)

        LocalIdGenerator(mainDatabase).generateLocalId(EntityName.Game) shouldBe 6
    }

    @Test
    fun `It should generate sequential IDs`() {
        val generator = LocalIdGenerator(mainDatabase)
        insertGame(localId = 25)

        val idOne = generator.generateLocalId(EntityName.Game)
        val idTwo = generator.generateLocalId(EntityName.Game)
        val idThree = generator.generateLocalId(EntityName.Game)

        idOne shouldBe 26
        idTwo shouldBe 27
        idThree shouldBe 28
    }

    @Test
    fun `It should keep track of different entities`() {
        val generator = LocalIdGenerator(mainDatabase)
        insertGame(localId = 5)
        insertDartsMatch(localId = 25)

        generator.generateLocalId(EntityName.Game) shouldBe 6
        generator.generateLocalId(EntityName.DartsMatch) shouldBe 26
        generator.generateLocalId(EntityName.Game) shouldBe 7
        generator.generateLocalId(EntityName.DartsMatch) shouldBe 27
    }

    @Test
    fun `It should be thread-safe`() {
        val generator = LocalIdGenerator(mainDatabase)
        generator.generateLocalId(EntityName.Game)

        val threads = mutableListOf<Thread>()
        val runnables = mutableListOf<IdGeneratorRunnable>()
        repeat(10) {
            val runnable = IdGeneratorRunnable(generator, mutableListOf())
            runnables.add(runnable)

            threads.add(Thread(runnable))
        }

        threads.forEach { it.start() }

        threads.forEach { it.join() }

        val allIds = mutableListOf<Long>()
        runnables.forEach { allIds.addAll(it.list) }

        allIds.shouldHaveSize(200)
        allIds.shouldBeUnique()
    }

    @Test
    fun `Should clear the cache, generating correct local IDs afterwards`() {
        val generator = LocalIdGenerator(mainDatabase)
        generator.generateLocalId(EntityName.Game) shouldBe 1L

        insertGame(localId = 2L)
        insertGame(localId = 3L)

        generator.clearCache()
        generator.generateLocalId(EntityName.Game) shouldBe 4L
    }

    private class IdGeneratorRunnable(
        val generator: LocalIdGenerator,
        val list: MutableList<Long>
    ) : Runnable {
        override fun run() {
            repeat(20) {
                val id = generator.generateLocalId(EntityName.Game)
                list.add(id)
            }
        }
    }
}
