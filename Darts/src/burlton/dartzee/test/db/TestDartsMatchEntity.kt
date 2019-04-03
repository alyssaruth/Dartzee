package burlton.dartzee.test.db

import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.db.DartsMatchEntity
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.test.helper.*
import burlton.desktopcore.code.util.getSqlDateNow
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test

class TestDartsMatchEntity: AbstractEntityTest<DartsMatchEntity>()
{
    override fun factoryDao() = DartsMatchEntity()

    @Test
    fun `LocalId field should be unique`()
    {
        wipeTable("DartsMatch")

        insertDartsMatch(localId = 5)
        exceptionLogged() shouldBe false

        insertDartsMatch(localId = 5)
        exceptionLogged() shouldBe true
        getLogs().shouldContain("duplicate key")

        getCountFromTable("DartsMatch") shouldBe 1
    }

    @Test
    fun `LocalIds should be assigned along with RowId`()
    {
        wipeTable("DartsMatch")

        val entity = DartsMatchEntity()
        entity.assignRowId()

        entity.rowId.shouldNotBeEmpty()
        entity.localId shouldBe 1
    }

    @Test
    fun `Should correctly report whether a FIRST_TO match is complete`()
    {
        val match = DartsMatchEntity.factoryFirstTo(2)

        match.isComplete() shouldBe false

        val gameOne = GameEntity.factoryAndSave(match)
        val gameTwo = GameEntity.factoryAndSave(match)
        val gameThree = GameEntity.factoryAndSave(match)
        val playerOneId = insertPlayer()
        val playerTwoId = insertPlayer()

        insertParticipant(gameId = gameOne.rowId, finishingPosition = 1, playerId = playerOneId)
        insertParticipant(gameId = gameOne.rowId, finishingPosition = 2, playerId = playerTwoId)
        match.isComplete() shouldBe false

        insertParticipant(gameId = gameTwo.rowId, finishingPosition = 1, playerId = playerTwoId)
        insertParticipant(gameId = gameTwo.rowId, finishingPosition = 2, playerId = playerOneId)
        match.isComplete() shouldBe false

        insertParticipant(gameId = gameThree.rowId, finishingPosition = 2, playerId = playerTwoId)
        match.isComplete() shouldBe false

        insertParticipant(gameId = gameThree.rowId, finishingPosition = 1, playerId = playerOneId)
        match.isComplete() shouldBe true
    }

    @Test
    fun `Should log a SQLException if SQL fails checking whether a FIRST_TO match is complete`()
    {
        val match = DartsMatchEntity()
        match.mode = DartsMatchEntity.MODE_FIRST_TO
        match.rowId = "'"

        match.isComplete() shouldBe false
        exceptionLogged() shouldBe true
    }

    @Test
    fun `Should log a SQLException if SQL fails checking whether a POINTS match is complete`()
    {
        val match = DartsMatchEntity()
        match.mode = DartsMatchEntity.MODE_POINTS
        match.rowId = "'"
        match.games = 2

        match.isComplete() shouldBe false
        exceptionLogged() shouldBe true
    }

    @Test
    fun `Should stacktrace and return false for an unknown match type`()
    {
        val match = DartsMatchEntity()

        match.isComplete() shouldBe false
        exceptionLogged() shouldBe true
        getLogs() shouldContain("Unimplemented for match mode [-1]")
    }

    @Test
    fun `Should correctly report whether a POINTS match is complete`()
    {
        val match = DartsMatchEntity.factoryPoints(2, "")

        match.isComplete() shouldBe false

        insertGame(dartsMatchId = match.rowId, dtFinish = getSqlDateNow())
        match.isComplete() shouldBe false

        val gameTwo = GameEntity.factoryAndSave(match)
        match.isComplete() shouldBe false

        gameTwo.dtFinish = getSqlDateNow()
        gameTwo.saveToDatabase()

        match.isComplete() shouldBe true
    }

}