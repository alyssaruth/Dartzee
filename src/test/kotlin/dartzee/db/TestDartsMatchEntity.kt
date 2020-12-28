package dartzee.db

import dartzee.core.obj.HashMapCount
import dartzee.core.util.getSqlDateNow
import dartzee.db.DartsMatchEntity.Companion.constructPointsXml
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.helper.*
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.exceptions.WrappedSqlException
import dartzee.utils.InjectedThings
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.numerics.shouldBeBetween
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotBeEmpty
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import org.junit.jupiter.api.Test

class TestDartsMatchEntity: AbstractEntityTest<DartsMatchEntity>()
{
    override fun factoryDao() = DartsMatchEntity()

    @Test
    fun `LocalId field should be unique`()
    {
        insertDartsMatch(localId = 5)
        verifyNoLogs(CODE_SQL_EXCEPTION)

        val ex = shouldThrow<WrappedSqlException> {
            insertDartsMatch(localId = 5)
        }

        val sqle = ex.sqlException
        sqle.message shouldContain "duplicate key"

        getCountFromTable("DartsMatch") shouldBe 1
    }

    @Test
    fun `LocalIds should be assigned along with RowId`()
    {
        val entity = DartsMatchEntity()
        entity.assignRowId()

        entity.rowId.shouldNotBeEmpty()
        entity.localId shouldBe 1
    }

    @Test
    fun `Should reassign localId when merging into another database`()
    {
        usingInMemoryDatabase(withSchema = true) { otherDatabase ->
            insertDartsMatch(database = otherDatabase)
            insertDartsMatch(database = otherDatabase)

            val dartsMatch = insertDartsMatch(database = InjectedThings.mainDatabase)
            dartsMatch.localId shouldBe 1

            dartsMatch.mergeIntoDatabase(otherDatabase)
            dartsMatch.localId shouldBe 3

            val retrieved = DartsMatchEntity(otherDatabase).retrieveForId(dartsMatch.rowId)!!
            retrieved.localId shouldBe 3
        }
    }

    @Test
    fun `Should correctly report whether a FIRST_TO match is complete`()
    {
        val match = DartsMatchEntity.factoryFirstTo(2)

        match.isComplete() shouldBe false

        val gameOne = GameEntity.factoryAndSave(match)
        val gameTwo = GameEntity.factoryAndSave(match)
        val gameThree = GameEntity.factoryAndSave(match)
        val playerOneId = insertPlayer().rowId
        val playerTwoId = insertPlayer().rowId

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
        dm.mode = MatchMode.FIRST_TO
        dm.gameType = GameType.X01
        dm.gameParams = "501"

        dm.getMatchDesc() shouldBe "Match #1 (First to 3 - 501, 0 players)"
    }

    @Test
    fun `POINTS descriptions`()
    {
        val dm = DartsMatchEntity()
        dm.localId = 1
        dm.games = 3
        dm.mode = MatchMode.POINTS
        dm.gameType = GameType.GOLF
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
    fun `Should return the correct points per position in POINTS mode`()
    {
        val matchParams = constructPointsXml(15, 9, 6, 3, 2, 1)
        val dm = DartsMatchEntity.factoryPoints(3, matchParams)

        dm.getScoreForFinishingPosition(1) shouldBe 15
        dm.getScoreForFinishingPosition(2) shouldBe 9
        dm.getScoreForFinishingPosition(3) shouldBe 6
        dm.getScoreForFinishingPosition(4) shouldBe 3
        dm.getScoreForFinishingPosition(5) shouldBe 2
        dm.getScoreForFinishingPosition(6) shouldBe 1
        dm.getScoreForFinishingPosition(-1) shouldBe 0
    }

    @Test
    fun `Should increment the ordinal in place and return it`()
    {
        val match = DartsMatchEntity()
        match.incrementAndGetCurrentOrdinal() shouldBe 1
        match.incrementAndGetCurrentOrdinal() shouldBe 2
        match.incrementAndGetCurrentOrdinal() shouldBe 3
    }

    @Test
    fun `Should flip the order if there are only 2 players`()
    {
        val match = DartsMatchEntity()

        val bob = factoryPlayer("Bob")

        val amy = factoryPlayer("Amy")

        match.players = mutableListOf(bob, amy)

        for (i in 0..20)
        {
            match.shufflePlayers()
            match.players.shouldContainExactly(amy, bob)

            match.shufflePlayers()
            match.players.shouldContainExactly(bob, amy)
        }
    }

    @Test
    fun `Should shuffle fairly with more than 2 players`()
    {
        val players = mutableListOf(factoryPlayer("Alice"),
                factoryPlayer("Bob"),
                factoryPlayer("Clive"),
                factoryPlayer("Donna"))

        val match = DartsMatchEntity()
        match.players = players


        val hmOrderToCount = HashMapCount<String>()
        for (i in 1..2400000)
        {
            match.shufflePlayers()

            hmOrderToCount.incrementCount("${match.players}")
        }

        hmOrderToCount.size shouldBe 24 //Should have all permutations at least once

        hmOrderToCount.values.forEach{
            it.shouldBeBetween(98000, 102000)
        }
    }

    @Test
    fun `Should cache metadata from a game correctly`()
    {
        val game501 = GameEntity.factoryAndSave(GameType.X01, "501")
        game501.matchOrdinal = 2
        val gameGolf = GameEntity.factoryAndSave(GameType.GOLF, "18")
        gameGolf.matchOrdinal = 4

        insertPlayerForGame("BTBF", game501.rowId)
        insertPlayerForGame("Mooch", game501.rowId)

        insertPlayerForGame("Scat", gameGolf.rowId)
        insertPlayerForGame("Aggie", gameGolf.rowId)

        val match = DartsMatchEntity()

        match.cacheMetadataFromGame(game501)
        match.gameType shouldBe game501.gameType
        match.gameParams shouldBe game501.gameParams
        match.incrementAndGetCurrentOrdinal() shouldBe game501.matchOrdinal + 1
        match.players.map{it.name}.shouldContainExactlyInAnyOrder("BTBF", "Mooch")

        match.cacheMetadataFromGame(gameGolf)
        match.gameType shouldBe gameGolf.gameType
        match.gameParams shouldBe gameGolf.gameParams
        match.incrementAndGetCurrentOrdinal() shouldBe gameGolf.matchOrdinal + 1
        match.players.map{it.name}.shouldContainExactlyInAnyOrder("Scat", "Aggie")
    }

    @Test
    fun `Should save a first-to match correctly`()
    {
        val dm = DartsMatchEntity.factoryFirstTo(3)

        val retrievedDm = dm.retrieveForId(dm.rowId)!!
        retrievedDm.games shouldBe 3
        retrievedDm.matchParams shouldBe ""
        retrievedDm.mode shouldBe MatchMode.FIRST_TO
        retrievedDm.localId shouldNotBe -1
    }

    @Test
    fun `Should save a points match correctly`()
    {
        val dm = DartsMatchEntity.factoryPoints(3, "foo")

        val retrievedDm = dm.retrieveForId(dm.rowId)!!
        retrievedDm.games shouldBe 3
        retrievedDm.matchParams shouldBe "foo"
        retrievedDm.mode shouldBe MatchMode.POINTS
        retrievedDm.localId shouldNotBe -1
    }
}