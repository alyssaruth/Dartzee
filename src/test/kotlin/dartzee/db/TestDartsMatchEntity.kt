package dartzee.db

import dartzee.db.DartsMatchEntity.Companion.constructPointsJson
import dartzee.game.GameType
import dartzee.game.MatchMode
import dartzee.helper.DEFAULT_X01_CONFIG
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDartsMatch
import dartzee.helper.insertGame
import dartzee.helper.usingInMemoryDatabase
import dartzee.logging.CODE_SQL_EXCEPTION
import dartzee.logging.exceptions.WrappedSqlException
import dartzee.utils.InjectedThings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import org.junit.jupiter.api.Test

class TestDartsMatchEntity : AbstractEntityTest<DartsMatchEntity>() {
    override fun factoryDao() = DartsMatchEntity()

    @Test
    fun `LocalId field should be unique`() {
        insertDartsMatch(localId = 5)
        verifyNoLogs(CODE_SQL_EXCEPTION)

        val ex = shouldThrow<WrappedSqlException> { insertDartsMatch(localId = 5) }

        val sqle = ex.sqlException
        sqle.message shouldContain "duplicate key"

        getCountFromTable("DartsMatch") shouldBe 1
    }

    @Test
    fun `LocalIds should be assigned along with RowId`() {
        val entity = DartsMatchEntity()
        entity.assignRowId()

        entity.rowId.shouldNotBeEmpty()
        entity.localId shouldBe 1
    }

    @Test
    fun `Should reassign localId when merging into another database`() {
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
    fun `FIRST_TO descriptions`() {
        val dm = DartsMatchEntity()
        dm.localId = 1
        dm.games = 3
        dm.mode = MatchMode.FIRST_TO
        dm.gameType = GameType.X01
        dm.gameParams = DEFAULT_X01_CONFIG.toJson()

        dm.getMatchDesc() shouldBe "Match #1 (First to 3 - 501)"
    }

    @Test
    fun `POINTS descriptions`() {
        val dm = DartsMatchEntity()
        dm.localId = 1
        dm.games = 3
        dm.mode = MatchMode.POINTS
        dm.gameType = GameType.GOLF
        dm.gameParams = "18"

        dm.getMatchDesc() shouldBe "Match #1 (Points based (3 games) - Golf - 18 holes)"
    }

    @Test
    fun `Should only return 1 point for 1st place in FIRST_TO`() {
        val dm = DartsMatchEntity.factoryFirstTo(3)

        dm.getScoreForFinishingPosition(1) shouldBe 1
        dm.getScoreForFinishingPosition(2) shouldBe 0
        dm.getScoreForFinishingPosition(3) shouldBe 0
        dm.getScoreForFinishingPosition(4) shouldBe 0
        dm.getScoreForFinishingPosition(-1) shouldBe 0
    }

    @Test
    fun `Should return the correct points per position in POINTS mode`() {
        val matchParams = constructPointsJson(15, 9, 6, 3, 2, 1)
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
    fun `Should cache metadata from a game correctly`() {
        val game501 = insertGame(gameType = GameType.X01, gameParams = "301")
        val gameGolf = insertGame(gameType = GameType.GOLF, gameParams = "18")

        val match = DartsMatchEntity()

        match.cacheMetadataFromGame(game501)
        match.gameType shouldBe game501.gameType
        match.gameParams shouldBe game501.gameParams

        match.cacheMetadataFromGame(gameGolf)
        match.gameType shouldBe gameGolf.gameType
        match.gameParams shouldBe gameGolf.gameParams
    }

    @Test
    fun `Should save a first-to match correctly`() {
        val dm = DartsMatchEntity.factoryFirstTo(3)

        val retrievedDm = dm.retrieveForId(dm.rowId)!!
        retrievedDm.games shouldBe 3
        retrievedDm.matchParams shouldBe ""
        retrievedDm.mode shouldBe MatchMode.FIRST_TO
        retrievedDm.localId shouldNotBe -1
    }

    @Test
    fun `Should save a points match correctly`() {
        val dm = DartsMatchEntity.factoryPoints(3, "foo")

        val retrievedDm = dm.retrieveForId(dm.rowId)!!
        retrievedDm.games shouldBe 3
        retrievedDm.matchParams shouldBe "foo"
        retrievedDm.mode shouldBe MatchMode.POINTS
        retrievedDm.localId shouldNotBe -1
    }
}
