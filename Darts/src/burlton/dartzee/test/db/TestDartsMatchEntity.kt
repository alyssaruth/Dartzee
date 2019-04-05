package burlton.dartzee.test.db

import burlton.core.test.helper.exceptionLogged
import burlton.core.test.helper.getLogs
import burlton.dartzee.code.db.*
import burlton.dartzee.code.db.DartsMatchEntity.Companion.constructPointsXml
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

    @Test
    fun `Should return the number of players in the match`()
    {
        val dm = DartsMatchEntity()
        dm.getPlayerCount() shouldBe 0

        dm.players.add(PlayerEntity())
        dm.getPlayerCount() shouldBe 1
    }

    @Test
    fun `FIRST_TO descriptions`()
    {
        val dm = DartsMatchEntity()
        dm.localId = 1
        dm.games = 3
        dm.mode = DartsMatchEntity.MODE_FIRST_TO
        dm.gameType = GAME_TYPE_X01
        dm.gameParams = "501"

        dm.getMatchDesc() shouldBe "Match #1 (First to 3 - 501, 0 players)"
    }

    @Test
    fun `POINTS descriptions`()
    {
        val dm = DartsMatchEntity()
        dm.localId = 1
        dm.games = 3
        dm.mode = DartsMatchEntity.MODE_POINTS
        dm.gameType = GAME_TYPE_GOLF
        dm.gameParams = "18"

        dm.getMatchDesc() shouldBe "Match #1 (Points based (3 games) - Golf - 18 holes, 0 players)"
    }

    @Test
    fun `Should only return 1 point for 1st place in FIRST_TO`()
    {
        val dm = DartsMatchEntity.factoryFirstTo(3)

        dm.getScoreForFinishingPosition(1) shouldBe 1
        dm.getScoreForFinishingPosition(2) shouldBe 0
        dm.getScoreForFinishingPosition(3) shouldBe 0
        dm.getScoreForFinishingPosition(4) shouldBe 0
        dm.getScoreForFinishingPosition(-1) shouldBe 0
    }

    @Test
    fun `Should only return the correct points per position in POINTS mode`()
    {
        val matchParams = constructPointsXml(10, 6, 3, 1)
        val dm = DartsMatchEntity.factoryPoints(3, matchParams)

        dm.getScoreForFinishingPosition(1) shouldBe 10
        dm.getScoreForFinishingPosition(2) shouldBe 6
        dm.getScoreForFinishingPosition(3) shouldBe 3
        dm.getScoreForFinishingPosition(4) shouldBe 1
        dm.getScoreForFinishingPosition(-1) shouldBe 0
    }
}