package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.insertTeam
import dartzee.helper.retrieveAchievement
import dartzee.`object`.Dart
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.getSortedDartStr
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01Chucklevision :
    AbstractMultiRowAchievementTest<AchievementX01Chucklevision>() {
    override fun factoryAchievement() = AchievementX01Chucklevision()

    private fun makeChucklevisionDarts() = listOf(Dart(20, 3), Dart(5, 1), Dart(4, 1))

    @Test
    fun `Should include rounds that were completed as part of a team`() {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val team = insertTeam(gameId = g.rowId)
        val pt = insertParticipant(gameId = g.rowId, teamId = team.rowId, playerId = p.rowId)

        insertDartsForPlayer(g, p, makeChucklevisionDarts(), participant = pt)

        runConversion()

        getAchievementCount() shouldBe 1
    }

    @Test
    fun `Should include rounds with misses`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(1, 0), Dart(20, 3), Dart(3, 3)))
        insertDartsForPlayer(g, p, listOf(Dart(20, 3), Dart(1, 0), Dart(3, 3)))
        insertDartsForPlayer(g, p, listOf(Dart(20, 3), Dart(3, 3), Dart(1, 0)))

        runConversion()

        getAchievementCount() shouldBe 1

        val a = retrieveAchievement()
        a.achievementDetail shouldBe "T20, T3"
    }

    @Test
    fun `Should include rounds where only two darts were thrown`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 3), Dart(3, 3)))

        runConversion()

        getAchievementCount() shouldBe 1

        val a = retrieveAchievement()
        a.achievementDetail shouldBe "T20, T3"
    }

    @Test
    fun `Should ignore rounds that bust the player`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, makeChucklevisionDarts(), 69)
        insertDartsForPlayer(g, p, makeChucklevisionDarts(), 70)

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore rounds that do not add up to 69`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 3), Dart(3, 3), Dart(1, 1)))

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore rounds that do not add up to 69, in a game where another round does add up to 69`() {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDartsForPlayer(
            g,
            p,
            listOf(Dart(20, 3), Dart(3, 3), Dart(1, 1)),
            participant = pt,
            roundNumber = 1,
        )

        insertDartsForPlayer(g, p, makeChucklevisionDarts(), participant = pt, roundNumber = 2)

        runConversion()

        getAchievementCount() shouldBe 1
    }

    @Test
    fun `Should ignore permutations of the same method, and track the earliest one`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, makeChucklevisionDarts())

        Thread.sleep(200)

        insertDartsForPlayer(insertRelevantGame(), p, makeChucklevisionDarts().shuffled())
        insertDartsForPlayer(insertRelevantGame(), p, makeChucklevisionDarts().shuffled())
        insertDartsForPlayer(insertRelevantGame(), p, makeChucklevisionDarts().shuffled())
        insertDartsForPlayer(insertRelevantGame(), p, makeChucklevisionDarts().shuffled())
        insertDartsForPlayer(insertRelevantGame(), p, makeChucklevisionDarts().shuffled())

        runConversion()

        getAchievementCount() shouldBe 1

        val a = AchievementEntity.retrieveAchievement(AchievementType.X01_CHUCKLEVISION, p.rowId)!!
        a.gameIdEarned shouldBe g.rowId
    }

    @Test
    fun `Should insert a row for each valid permutation, and should match front end format`() {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val validOne = makeChucklevisionDarts()
        val validTwo = listOf(Dart(19, 3), Dart(10, 1), Dart(2, 1))

        insertDartsForPlayer(g, p, validOne)
        insertDartsForPlayer(g, p, validTwo)

        runConversion()

        getAchievementCount() shouldBe 2

        val achievements = AchievementEntity().retrieveEntities("PlayerId = '${p.rowId}'")
        val methods = achievements.map { it.achievementDetail }

        methods.shouldContainExactlyInAnyOrder(
            getSortedDartStr(validOne),
            getSortedDartStr(validTwo),
        )
    }

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database,
    ) {
        insertStandardChucklevision(p, g, database)
    }

    private fun insertStandardChucklevision(
        p: PlayerEntity,
        g: GameEntity,
        database: Database = mainDatabase,
    ) {
        insertDartsForPlayer(g, p, makeChucklevisionDarts(), database = database)
    }

    private fun insertDartsForPlayer(
        g: GameEntity,
        p: PlayerEntity,
        darts: List<Dart>,
        startingScore: Int = 501,
        database: Database = mainDatabase,
        participant: ParticipantEntity? = null,
        roundNumber: Int = 1,
    ) {
        val pt =
            participant
                ?: insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        var currentScore = startingScore
        darts.forEachIndexed { ix, drt ->
            insertDart(
                pt,
                score = drt.score,
                multiplier = drt.multiplier,
                ordinal = ix + 1,
                startingScore = currentScore,
                roundNumber = roundNumber,
                database = database,
            )
            currentScore -= drt.getTotal()
        }
    }
}
