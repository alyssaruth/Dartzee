package burlton.dartzee.test.achievements

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS
import burlton.dartzee.code.achievements.AchievementX01CheckoutCompleteness
import burlton.dartzee.code.db.*
import burlton.dartzee.test.helper.*
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test

class TestAchievementX01CheckoutCompleteness: TestAbstractAchievementRowPerGame<AchievementX01CheckoutCompleteness>()
{
    override fun factoryAchievement() = AchievementX01CheckoutCompleteness()

    @Test
    fun `Should ignore games of the wrong type`()
    {
        val g = insertGame(gameType = GAME_TYPE_GOLF)
        insertCheckout(insertPlayer(), g, 1)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should ignore non-checkout darts`()
    {
        val g = insertRelevantGame()
        val p = insertPlayer()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, startingScore = 100, score = 1, multiplier = 2)
        insertDart(roundId = rnd.rowId, startingScore = 2, score = 2, multiplier = 1)

        factoryAchievement().populateForConversion("")

        getCountFromTable("Achievement") shouldBe 0
    }

    @Test
    fun `Should insert one row for the earliest instance of the same checkout`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertCheckout(p, g, 5)

        Thread.sleep(200)

        val g2 = insertRelevantGame()
        insertCheckout(p, g2, 5)

        factoryAchievement().populateForConversion("")

        val a = AchievementEntity().retrieveEntity("PlayerId = '${p.rowId}'")!!
        a.gameIdEarned shouldBe g.rowId
        a.achievementRef shouldBe ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS
        a.achievementCounter shouldBe 5
    }

    @Test
    fun `Should insert a row per distinct checkout`()
    {
        val p = insertPlayer()
        val g = insertRelevantGame()

        insertCheckout(p, g, 5)
        insertCheckout(p, g, 1)
        insertCheckout(p, g, 5)
        insertCheckout(p, g, 2)
        insertCheckout(p, g, 5)
        insertCheckout(p, g, 2)

        factoryAchievement().populateForConversion("")

        val achievements = AchievementEntity().retrieveEntities("PlayerId = '${p.rowId}'")
        val scores = achievements.map{ it.achievementCounter }

        scores.shouldContainExactlyInAnyOrder(1, 2, 5)
    }

    override fun setUpAchievementRowForPlayer(p: PlayerEntity)
    {
        val g = insertRelevantGame()
        insertCheckout(p, g, 1)
    }

    private fun insertRelevantGame() = insertGame(gameType = GAME_TYPE_X01)
    private fun insertCheckout(p: PlayerEntity, g: GameEntity, score: Int = 1)
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, startingScore = score*2, score = score, multiplier = 2)
    }
}