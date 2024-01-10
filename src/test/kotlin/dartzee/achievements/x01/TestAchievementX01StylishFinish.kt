package dartzee.achievements.x01

import dartzee.achievements.AbstractMultiRowAchievementTest
import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.db.ParticipantEntity
import dartzee.db.PlayerEntity
import dartzee.helper.insertDart
import dartzee.helper.insertParticipant
import dartzee.helper.makeX01Round
import dartzee.`object`.Dart
import dartzee.utils.Database
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestAchievementX01StylishFinish :
    AbstractMultiRowAchievementTest<AchievementX01StylishFinish>() {
    override fun factoryAchievement() = AchievementX01StylishFinish()

    override fun setUpAchievementRowForPlayerAndGame(
        p: PlayerEntity,
        g: GameEntity,
        database: Database
    ) {
        val pt = insertParticipant(playerId = p.rowId, gameId = g.rowId, database = database)
        val round = makeX01Round(101, 1, Dart(19, 3), Dart(4, 1), Dart(20, 2))
        round.forEach { insertDart(pt, it, database = database) }
    }

    @Test
    fun `Should populate achievement row with correct details`() {
        val pt = insertParticipantAndDarts(101, Dart(20, 3), Dart(11, 1), Dart(15, 2))
        runConversion()

        val a =
            AchievementEntity.retrieveAchievement(AchievementType.X01_STYLISH_FINISH, pt.playerId)!!
        a.achievementCounter shouldBe 101
        a.achievementDetail shouldBe "T20, 11, D15"
        a.gameIdEarned shouldBe pt.gameId
    }

    @Test
    fun `Should cope with two dart finishes`() {
        val pt = insertParticipantAndDarts(55, Dart(5, 3), Dart(20, 2))
        runConversion()

        val a =
            AchievementEntity.retrieveAchievement(AchievementType.X01_STYLISH_FINISH, pt.playerId)!!
        a.achievementCounter shouldBe 55
        a.achievementDetail shouldBe "T5, D20"
        a.gameIdEarned shouldBe pt.gameId
    }

    @Test
    fun `Should ignore single dart finishes`() {
        insertParticipantAndDarts(40, Dart(20, 2))
        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore exact busts to 0`() {
        insertParticipantAndDarts(55, Dart(5, 2), Dart(15, 3))
        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore rounds that are not finishes`() {
        insertParticipantAndDarts(100, Dart(5, 3), Dart(15, 1), Dart(20, 2))
        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore finishes that are not stylish`() {
        insertParticipantAndDarts(80, Dart(20, 1), Dart(20, 1), Dart(20, 2))
        runConversion()

        getAchievementCount() shouldBe 0
    }

    @Test
    fun `Should ignore two dart finishes that are not stylish`() {
        insertParticipantAndDarts(60, Dart(20, 1), Dart(20, 2))
        runConversion()

        getAchievementCount() shouldBe 0
    }

    private fun insertParticipantAndDarts(
        startingScore: Int,
        vararg darts: Dart
    ): ParticipantEntity {
        val pt = insertRelevantParticipant()
        val round = makeX01Round(startingScore, 1, *darts)
        round.forEach { insertDart(pt, it) }

        return pt
    }
}
