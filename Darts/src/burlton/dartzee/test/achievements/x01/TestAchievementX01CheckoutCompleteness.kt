package burlton.dartzee.test.achievements.x01

import burlton.dartzee.code.achievements.ACHIEVEMENT_REF_X01_CHECKOUT_COMPLETENESS
import burlton.dartzee.code.achievements.x01.AchievementX01CheckoutCompleteness
import burlton.dartzee.code.db.AchievementEntity
import burlton.dartzee.code.db.GAME_TYPE_X01
import burlton.dartzee.code.db.GameEntity
import burlton.dartzee.code.db.PlayerEntity
import burlton.dartzee.test.achievements.TestAbstractAchievementRowPerGame
import burlton.dartzee.test.helper.*
import burlton.desktopcore.code.util.getSqlDateNow
import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp

class TestAchievementX01CheckoutCompleteness: TestAbstractAchievementRowPerGame<AchievementX01CheckoutCompleteness>()
{
    override val gameType = GAME_TYPE_X01

    override fun factoryAchievement() = AchievementX01CheckoutCompleteness()

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
        insertCheckout(p, g, 5, Timestamp(500))

        val g2 = insertRelevantGame()
        insertCheckout(p, g2, 5, Timestamp(1000))

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

        insertCheckout(p, g, 5, Timestamp(500))
        insertCheckout(p, g, 1, Timestamp(1000))
        insertCheckout(p, g, 5, Timestamp(1500))
        insertCheckout(p, g, 2, Timestamp(2000))
        insertCheckout(p, g, 5, Timestamp(2500))
        insertCheckout(p, g, 2, Timestamp(3000))

        factoryAchievement().populateForConversion("")

        val achievements = AchievementEntity().retrieveEntities("PlayerId = '${p.rowId}'")
        val scores = achievements.map{ it.achievementCounter }

        scores.shouldContainExactlyInAnyOrder(1, 2, 5)
    }

    override fun setUpAchievementRowForPlayerAndGame(p: PlayerEntity, g: GameEntity)
    {
        insertCheckout(p, g, 1)
    }

    private fun insertCheckout(p: PlayerEntity, g: GameEntity, score: Int = 1, dtCreation: Timestamp = getSqlDateNow())
    {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId)
        val rnd = insertRound(participantId = pt.rowId)

        insertDart(roundId = rnd.rowId, startingScore = score*2, score = score, multiplier = 2, dtCreation = dtCreation)
    }
}