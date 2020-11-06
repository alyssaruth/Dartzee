package dartzee.achievements.x01

import dartzee.`object`.Dart
import dartzee.achievements.ACHIEVEMENT_REF_X01_HOTEL_INSPECTOR
import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.helper.getCountFromTable
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.utils.Database
import dartzee.utils.InjectedThings.mainDatabase
import dartzee.utils.getSortedDartStr
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01HotelInspector: AbstractMultiRowAchievementTest<AchievementX01HotelInspector>()
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

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that bust the player`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)), 26)
        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)), 27)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that do not add up to 26`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(2, 1)))

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore rounds that do not add up to 26, in a game where another round does add up to 26`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(2, 1)),
            participant = pt, roundNumber = 1)

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)),
            participant = pt, roundNumber = 2)

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 1
    }

    @Test
    fun `Should ignore rounds that contain fewer than 3 darts`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(6, 1)))

        factoryAchievement().populateForConversion(emptyList())

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

        factoryAchievement().populateForConversion(emptyList())

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

        factoryAchievement().populateForConversion(emptyList())

        getCountFromTable("Achievement") shouldBe 2

        val achievements = AchievementEntity().retrieveEntities("PlayerId = '${p.rowId}'")
        val methods = achievements.map{ it.achievementDetail }

        methods.shouldContainExactlyInAnyOrder(getSortedDartStr(validOne), getSortedDartStr(validTwo))
    }

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity, database: Database)
    {
        insertStandardBurltonConstant(p, g, database)
    }

    private fun insertStandardBurltonConstant(p: PlayerEntity, g: GameEntity, database: Database = mainDatabase)
    {
        insertDartsForPlayer(g, p, listOf(Dart(20, 1), Dart(5, 1), Dart(1, 1)), database = database)
    }

    private fun insertDartsForPlayer(g: GameEntity,
                                     p: PlayerEntity,
                                     darts: List<Dart>,
                                     startingScore: Int = 501,
                                     database: Database = mainDatabase,
                                     participant: ParticipantEntity? = null,
                                     roundNumber: Int = 1)
    {
        val pt = participant ?: insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)

        var currentScore = startingScore
        darts.forEachIndexed { ix, drt ->
            insertDart(pt, score = drt.score, multiplier = drt.multiplier, ordinal = ix+1, startingScore = currentScore, roundNumber = roundNumber, database = database)
            currentScore -= drt.getTotal()
        }

    }
}