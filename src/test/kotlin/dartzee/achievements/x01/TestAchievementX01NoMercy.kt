package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.retrieveAchievement
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01NoMercy : AbstractMultiRowAchievementTest<AchievementX01NoMercy>() {
    override fun factoryAchievement() = AchievementX01NoMercy()

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database
    ) {
        val pt =
            insertParticipant(
                playerId = p.rowId,
                gameId = g.rowId,
                finalScore = 21,
                database = database
            )

        insertDart(pt, roundNumber = 7, startingScore = 7, ordinal = 1, database = database)
    }

    @Test
    fun `Should include participants who were part of a team`() {
        val pt = insertRelevantParticipant(team = true, finalScore = 21)
        val drt = insertDart(pt, roundNumber = 7, startingScore = 7, ordinal = 1)

        runConversion()
        getAchievementCount() shouldBe 1
        retrieveAchievement().dtAchieved shouldBe drt.dtCreation
    }

    @Test
    fun `Should not include data for an unfinished player`() {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(gameId = g.rowId, playerId = p.rowId, finalScore = -1)

        insertDart(pt, roundNumber = 1, startingScore = 7, ordinal = 1)

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should only count if the first dart has the right startingScore`() {
        val g = insertRelevantGame()
        val p = insertPlayer()

        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 21)
        insertDart(pt, roundNumber = 7, ordinal = 1, startingScore = 12, score = 5, multiplier = 1)
        insertDart(pt, roundNumber = 7, ordinal = 2, startingScore = 7, score = 3, multiplier = 1)

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should insert a row for every successful finish, even with the same startingScore`() {
        val alice = insertPlayer(name = "Alice")

        setUpAchievementRowForPlayer(alice)
        setUpAchievementRowForPlayer(alice)

        runConversion()

        getAchievementCount() shouldBe 2
    }

    @Test
    fun `Should count finishes of 3, 5, 7 and 9`() {
        val alice = insertPlayer(name = "Alice")

        setUpFinishForPlayer(alice, 3)
        setUpFinishForPlayer(alice, 5)
        setUpFinishForPlayer(alice, 7)
        setUpFinishForPlayer(alice, 9)

        runConversion()

        getAchievementCount() shouldBe 4
    }

    @Test
    fun `Should not count higher odd numbers, or even finishes`() {
        val alice = insertPlayer(name = "Alice")

        setUpFinishForPlayer(alice, 2)
        setUpFinishForPlayer(alice, 4)
        setUpFinishForPlayer(alice, 11)
        setUpFinishForPlayer(alice, 13)

        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should set the detail on the achievement row to be the finish that was attained`() {
        val p = insertPlayer()
        setUpFinishForPlayer(p, 5)

        runConversion()

        retrieveAchievement().achievementCounter shouldBe -1
        retrieveAchievement().achievementDetail shouldBe "5"
    }

    private fun setUpFinishForPlayer(p: PlayerEntity, finish: Int) {
        val g = insertRelevantGame()
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, finalScore = 21)

        insertDart(pt, roundNumber = 7, startingScore = finish, ordinal = 1)
    }
}
