package dartzee.achievements.x01

import dartzee.`object`.Dart
import dartzee.achievements.ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR
import dartzee.achievements.x01.AchievementX01HotelInspector
import dartzee.db.AchievementEntity
import dartzee.db.GAME_TYPE_X01
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.utils.getSortedDartStr
import dartzee.achievements.TestAbstractAchievementRowPerGame
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01HotelInspector: TestAbstractAchievementRowPerGame<AchievementX01HotelInspector>()
{
    override fun factoryAchievement() = AchievementX01HotelInspector()

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

    @Test
    fun `Should insert a row for each valid permutation, and should match front end format`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        val validOne = listOf(Dart(5, 1), Dart(20, 1), Dart(1, 1))
        val validTwo = listOf(Dart(5, 3), Dart(3, 2), Dart(5, 1))

        insertDartsForPlayer(g, p, validOne)
        insertDartsForPlayer(g, p, validTwo)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 2

        val achievements = AchievementEntity().retrieveEntities("PlayerId = '${p.rowId}'")
        val methods = achievements.map{ it.achievementDetail }

        methods.shouldContainExactlyInAnyOrder(getSortedDartStr(validOne), getSortedDartStr(validTwo))
    }

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        insertStandardBurltonConstant(p, g)
    }

    private fun insertStandardBurltonConstant(p: PlayerEntity, g: GameEntity)
    {
        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)))
    }

    private fun insertDartsForPlayer(g: GameEntity, p: PlayerEntity, darts: List<Dart>, startingScore: Int = 501)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        var currentScore = startingScore
        darts.forEachIndexed { ix, drt ->
            insertDart(pt, score = drt.score, multiplier = drt.multiplier, ordinal = ix+1, startingScore = currentScore)
            currentScore -= drt.getTotal()
        }

    }
}