package burlton.dartzee.test.db

import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.test.helper.getCountFromTable
import burlton.dartzee.test.helper.insertGame
import burlton.dartzee.test.helper.wipeTable
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import org.junit.Test

class TestGameEntity: AbstractEntityTest<GameEntity>()
{
    override fun factoryDao() = GameEntity()

    override fun beforeEachTest()
    {
        super.beforeEachTest()
        wipeTable("Game")
    }

    @Test
    fun `LocalId field should be unique`()
    {
        insertGame(localId = 5)
        exceptionLogged() shouldBe false

        insertGame(localId = 5)
        exceptionLogged() shouldBe true
        getLogs().shouldContain("duplicate key")

        getCountFromTable("Game") shouldBe 1
    }
}