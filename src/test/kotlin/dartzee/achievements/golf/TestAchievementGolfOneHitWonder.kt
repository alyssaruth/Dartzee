package dartzee.achievements.golf

import dartzee.achievements.AbstractAchievementTest
import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.PlayerEntity
import dartzee.drtDoubleFour
import dartzee.drtDoubleNineteen
import dartzee.drtDoubleOne
import dartzee.drtDoubleSeventeen
import dartzee.drtDoubleTwenty
import dartzee.drtDoubleTwo
import dartzee.drtInnerOne
import dartzee.drtInnerTwo
import dartzee.drtOuterOne
import dartzee.drtOuterThree
import dartzee.drtOuterTwo
import dartzee.drtTrebleOne
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.helper.makeGolfRound
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import java.sql.Timestamp
import org.junit.jupiter.api.Test

class TestAchievementGolfOneHitWonder : AbstractAchievementTest<AchievementGolfOneHitWonder>() {
    override fun factoryAchievement() = AchievementGolfOneHitWonder()

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database,
    ) {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)
        val dartRounds =
            listOf(
                makeGolfRound(1, listOf(drtOuterOne(), drtDoubleOne())),
                makeGolfRound(2, listOf(drtOuterTwo(), drtInnerTwo())),
                makeGolfRound(
                    3,
                    listOf(drtOuterThree(), drtDoubleNineteen(), drtDoubleSeventeen()),
                ),
                makeGolfRound(4, listOf(drtDoubleFour())),
            )

        dartRounds.flatten().forEach { insertDart(pt, it, database = database) }
    }

    @Test
    fun `Should ignore rounds where the double hit is not the correct one`() {
        val pt = insertRelevantParticipant()

        val dartRound = makeGolfRound(1, listOf(drtDoubleTwenty()))
        dartRound.forEach { insertDart(pt, it) }

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore rounds where a double was not thrown`() {
        val pt = insertRelevantParticipant()

        val dartRound = makeGolfRound(1, listOf(drtInnerOne(), drtOuterOne(), drtTrebleOne()))
        dartRound.forEach { insertDart(pt, it) }

        runConversion()
        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should pick the game with the highest count`() {
        val player = insertPlayer()
        val pt1 = insertRelevantParticipant(player)
        val pt2 = insertRelevantParticipant(player)

        val dartRounds1 = listOf(makeGolfRound(1, listOf(drtOuterOne(), drtDoubleOne())))
        dartRounds1.flatten().forEach { insertDart(pt1, it, dtCreation = Timestamp(500)) }

        val dartRounds2 =
            listOf(
                makeGolfRound(1, listOf(drtOuterOne(), drtDoubleOne())),
                makeGolfRound(2, listOf(drtDoubleTwo())),
            )
        dartRounds2.flatten().forEach { insertDart(pt2, it, dtCreation = Timestamp(1000)) }

        runConversion()

        val a =
            AchievementEntity.retrieveAchievement(
                AchievementType.GOLF_ONE_HIT_WONDER,
                player.rowId,
            )!!
        a.gameIdEarned shouldBe pt2.gameId
        a.dtAchieved shouldBe Timestamp(1000)
        a.achievementCounter shouldBe 2
    }

    @Test
    fun `Should take the earliest game where the record was achieved`() {
        val player = insertPlayer()
        val pt1 = insertRelevantParticipant(player)
        val pt2 = insertRelevantParticipant(player)

        val dartRound1 = makeGolfRound(1, listOf(drtOuterOne(), drtDoubleOne()))
        dartRound1.forEach { insertDart(pt1, it, dtCreation = Timestamp(500)) }

        val dartRound2 = makeGolfRound(1, listOf(drtOuterOne(), drtDoubleOne()))
        dartRound2.forEach { insertDart(pt2, it, dtCreation = Timestamp(1000)) }

        runConversion()

        val a =
            AchievementEntity.retrieveAchievement(
                AchievementType.GOLF_ONE_HIT_WONDER,
                player.rowId,
            )!!
        a.gameIdEarned shouldBe pt1.gameId
        a.dtAchieved shouldBe Timestamp(500)
        a.achievementCounter shouldBe 1
    }
}
