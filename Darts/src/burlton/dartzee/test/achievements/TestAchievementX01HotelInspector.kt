package burlton.dartzee.test.achievements

import burlton.dartzee.code.`object`.Dart
import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR
import burlton.dartzee.code.achievements.AchievementX01HotelInspector
import burlton.dartzee.code.db.*
import burlton.dartzee.test.helper.*
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01HotelInspector: TestAbstractAchievementRowPerGame<AchievementX01HotelInspector>()
{
    override fun factoryAchievement() = AchievementX01HotelInspector()

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val g = insertGame(gameType = GAME_TYPE_GOLF)
        insertStandardBurltonConstant(insertPlayer(), g)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that contain any misses`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(1, 0), Dart(20, 1), Dart(6, 1)))
        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(1, 0), Dart(6, 1)))
        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(6, 1), Dart(1, 0)))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that bust the player`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)), 26)
        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)), 27)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that do not add up to 26`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(2, 1)))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that contain fewer than 3 darts`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(6, 1)))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore permutations of the same method, and track the earliest one`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))

        Thread.sleep(200)

        insertDartsForPlayer(insertRelevantGame(), p, listOf(Dart(20, 1), Dart(1, 1), Dart(5, 1)))
        insertDartsForPlayer(insertRelevantGame(), p, listOf(Dart(5, 1), Dart(20, 1), Dart(1, 1)))
        insertDartsForPlayer(insertRelevantGame(), p, listOf(Dart(5, 1), Dart(1, 1), Dart(20, 1)))
        insertDartsForPlayer(insertRelevantGame(), p, listOf(Dart(1, 1), Dart(20, 1), Dart(5, 1)))
        insertDartsForPlayer(insertRelevantGame(), p, listOf(Dart(1, 1), Dart(5, 1), Dart(20, 1)))

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 1

        val a = AchievementEntity.retrieveAchievement(ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR, p.rowId)!!
        a.gameIdEarned shouldBe g.rowId
    }

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val g = insertRelevantGame()

        insertStandardBurltonConstant(p, g)
    }

    private fun insertStandardBurltonConstant(p: PlayerEntity, g: GameEntity)
    {
        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))
    }

    private fun insertDartsForPlayer(g: GameEntity, p: PlayerEntity, darts: List<Dart>, startingScore: Int = 501)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId, roundNumber = 1)

        var currentScore = startingScore
        darts.forEachIndexed { ix, drt ->
            insertDart(roundId = rnd.rowId, score = drt.score, multiplier = drt.multiplier, ordinal = ix+1, startingScore = currentScore)
            currentScore -= drt.getTotal()
        }

    }

    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_X01)
}