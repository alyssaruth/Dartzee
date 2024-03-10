package dartzee.screen.game

import dartzee.achievements.AchievementType
import dartzee.db.AchievementEntity
import dartzee.drtDoubleEight
import dartzee.drtDoubleFive
import dartzee.drtDoubleFour
import dartzee.drtDoubleNine
import dartzee.drtDoubleOne
import dartzee.drtDoubleSeven
import dartzee.drtDoubleSix
import dartzee.drtDoubleSixteen
import dartzee.drtDoubleThree
import dartzee.drtDoubleTwo
import dartzee.drtInnerFifteen
import dartzee.drtInnerNine
import dartzee.drtInnerSix
import dartzee.drtInnerThree
import dartzee.drtInnerTwo
import dartzee.drtMissSixteen
import dartzee.drtMissThirteen
import dartzee.drtOuterEighteen
import dartzee.drtOuterEleven
import dartzee.drtOuterFour
import dartzee.drtOuterFourteen
import dartzee.drtOuterOne
import dartzee.drtOuterSeventeen
import dartzee.drtOuterTen
import dartzee.drtOuterThirteen
import dartzee.drtOuterThree
import dartzee.drtOuterTwenty
import dartzee.drtTrebleFive
import dartzee.drtTrebleTwelve
import dartzee.drtTrebleTwo
import dartzee.helper.AbstractTest
import dartzee.helper.AchievementSummary
import dartzee.helper.insertAchievement
import dartzee.helper.preparePlayers
import dartzee.helper.randomGuid
import dartzee.helper.retrieveAchievementsForPlayer
import dartzee.`object`.Dart
import dartzee.`object`.SegmentType
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGamePanelGolf : AbstractTest() {
    @Test
    fun `It should not update gambler achievement for missed darts`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts =
            listOf(
                Dart(1, 0, segmentType = SegmentType.MISS),
                Dart(20, 3, segmentType = SegmentType.TREBLE),
                Dart(1, 1, segmentType = SegmentType.OUTER_SINGLE)
            )

        panel.addCompletedRound(darts)

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId) shouldBe
            null
    }

    @Test
    fun `It should sum up all the points gambled in that round`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts =
            listOf(
                Dart(1, 3, segmentType = SegmentType.TREBLE),
                Dart(1, 3, segmentType = SegmentType.OUTER_SINGLE),
                Dart(1, 1, segmentType = SegmentType.TREBLE)
            )

        panel.addCompletedRound(darts)

        val a =
            AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId)!!
        a.achievementCounter shouldBe 4
    }

    @Test
    fun `It should compute correctly when just two darts thrown`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts =
            listOf(
                Dart(1, 1, segmentType = SegmentType.OUTER_SINGLE),
                Dart(1, 1, segmentType = SegmentType.INNER_SINGLE)
            )

        panel.addCompletedRound(darts)

        val a =
            AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId)!!
        a.achievementCounter shouldBe 1
    }

    @Test
    fun `It should do nothing for a single dart hit`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(Dart(1, 1, segmentType = SegmentType.TREBLE))
        panel.addCompletedRound(darts)

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_POINTS_RISKED, playerId) shouldBe
            null
    }

    @Test
    fun `Should not count darts that aren't a hole in one`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(Dart(1, 3, segmentType = SegmentType.TREBLE))
        panel.addCompletedRound(darts)

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe
            null
    }

    @Test
    fun `Should not count darts for the wrong hole`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts = listOf(Dart(2, 2, segmentType = SegmentType.DOUBLE))
        panel.addCompletedRound(darts)

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe
            null
    }

    @Test
    fun `Should only count the last dart thrown`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)

        val darts =
            listOf(
                Dart(1, 2, segmentType = SegmentType.DOUBLE),
                Dart(1, 3, segmentType = SegmentType.TREBLE)
            )
        panel.addCompletedRound(darts)

        AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId) shouldBe
            null
    }

    @Test
    fun `Should insert a row for a new hole in one`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)
        insertAchievement(
            playerId = playerId,
            type = AchievementType.GOLF_COURSE_MASTER,
            achievementDetail = "2"
        )

        val darts = listOf(Dart(1, 2, segmentType = SegmentType.DOUBLE))
        panel.addCompletedRound(darts)

        val rows =
            AchievementEntity()
                .retrieveEntities(
                    "PlayerId = '$playerId' AND AchievementType = '${AchievementType.GOLF_COURSE_MASTER}'"
                )
        rows.size shouldBe 2
        rows.map { it.achievementDetail }.shouldContainExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `Should not insert a row for a hole in one already attained`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId)
        val originalRow =
            insertAchievement(
                playerId = playerId,
                type = AchievementType.GOLF_COURSE_MASTER,
                achievementDetail = "1"
            )

        val darts = listOf(Dart(1, 2, segmentType = SegmentType.DOUBLE))
        panel.addCompletedRound(darts)

        val newRow =
            AchievementEntity.retrieveAchievement(AchievementType.GOLF_COURSE_MASTER, playerId)!!
        newRow.rowId shouldBe originalRow.rowId
    }

    @Test
    fun `Should update one hit wonder achievement when record is exceeded`() {
        val playerId = randomGuid()
        val otherGameId = randomGuid()
        val panel = makeGolfGamePanel(playerId)
        val originalRow =
            insertAchievement(
                playerId = playerId,
                type = AchievementType.GOLF_ONE_HIT_WONDER,
                gameIdEarned = otherGameId,
                achievementCounter = 2
            )

        val roundOne = listOf(drtDoubleOne())
        val roundTwo = listOf(drtTrebleTwo(), drtDoubleTwo())
        panel.addCompletedRound(roundOne)
        panel.addCompletedRound(roundTwo)

        val currentRow =
            AchievementEntity.retrieveAchievement(AchievementType.GOLF_ONE_HIT_WONDER, playerId)!!
        currentRow.gameIdEarned shouldBe originalRow.gameIdEarned
        currentRow.achievementCounter shouldBe originalRow.achievementCounter

        panel.addCompletedRound(listOf(drtDoubleThree()))
        val newRow =
            AchievementEntity.retrieveAchievement(AchievementType.GOLF_ONE_HIT_WONDER, playerId)!!
        newRow.gameIdEarned shouldBe panel.gameEntity.rowId
        newRow.achievementCounter shouldBe 3
    }

    @Test
    fun `Should unlock the correct achievements for team play`() {
        val (p1, p2) = preparePlayers(2)
        val panel = makeGolfGamePanel(listOf(p1, p2), true, "18")
        val gameId = panel.gameEntity.rowId

        val roundOne =
            listOf(drtOuterOne(), drtOuterOne(), drtDoubleOne()) // P1: Risked 2, CM: 1, OHW: 1
        panel.addCompletedRound(roundOne)

        val roundTwo = listOf(drtDoubleTwo()) // P2: Risked 0, CM: 2, OHW: 1
        panel.addCompletedRound(roundTwo)

        val roundThree = listOf(drtInnerThree(), drtDoubleThree()) // P1: Risked 4, CM: 1, 3, OHW: 2
        panel.addCompletedRound(roundThree)

        retrieveAchievementsForPlayer(p1.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, gameId, "1"),
                AchievementSummary(AchievementType.GOLF_POINTS_RISKED, 2, gameId, "3"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, gameId, "1"),
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, gameId, "3"),
                AchievementSummary(AchievementType.GOLF_ONE_HIT_WONDER, 2, gameId)
            )

        retrieveAchievementsForPlayer(p2.rowId)
            .shouldContainExactlyInAnyOrder(
                AchievementSummary(AchievementType.GOLF_COURSE_MASTER, -1, gameId, "2"),
                AchievementSummary(AchievementType.GOLF_ONE_HIT_WONDER, 1, gameId)
            )
    }

    @Test
    fun `Should update In Bounds achievement`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId, gameParams = "18")

        panel.addCompletedRound(drtOuterOne())
        panel.addCompletedRound(drtInnerTwo())
        panel.addCompletedRound(drtOuterThree(), drtOuterThree())
        panel.addCompletedRound(drtOuterFour())
        panel.addCompletedRound(drtOuterTwenty(), drtTrebleFive())
        panel.addCompletedRound(drtInnerSix())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtInnerNine())
        panel.addCompletedRound(drtOuterTen())
        panel.addCompletedRound(drtOuterEleven())
        panel.addCompletedRound(drtTrebleTwelve())
        panel.addCompletedRound(drtMissThirteen(), drtMissThirteen(), drtOuterThirteen())
        panel.addCompletedRound(drtOuterFourteen())
        panel.addCompletedRound(drtInnerFifteen())
        panel.addCompletedRound(drtMissSixteen(), drtDoubleSixteen())
        panel.addCompletedRound(drtOuterSeventeen())
        panel.addCompletedRound(drtOuterEighteen())

        panel.gameEntity.isFinished() shouldBe true
        val a = AchievementEntity.retrieveAchievement(AchievementType.GOLF_IN_BOUNDS, playerId)!!
        a.gameIdEarned shouldBe panel.gameEntity.rowId
        a.achievementDetail shouldBe "55"
    }

    @Test
    fun `Should not update In Bounds achievement if a number is missed`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId, gameParams = "18")

        panel.addCompletedRound(drtOuterOne())
        panel.addCompletedRound(drtInnerTwo())
        panel.addCompletedRound(drtOuterThree(), drtOuterThree())
        panel.addCompletedRound(drtOuterFour())
        panel.addCompletedRound(drtOuterTwenty(), drtTrebleFive())
        panel.addCompletedRound(drtInnerSix())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtInnerNine())
        panel.addCompletedRound(drtOuterTen())
        panel.addCompletedRound(drtOuterEleven())
        panel.addCompletedRound(drtTrebleTwelve())
        panel.addCompletedRound(drtMissThirteen(), drtMissThirteen(), drtMissThirteen())
        panel.addCompletedRound(drtOuterFourteen())
        panel.addCompletedRound(drtInnerFifteen())
        panel.addCompletedRound(drtMissSixteen(), drtDoubleSixteen())
        panel.addCompletedRound(drtOuterSeventeen())
        panel.addCompletedRound(drtOuterEighteen())

        panel.gameEntity.isFinished() shouldBe true
        AchievementEntity.retrieveAchievement(AchievementType.GOLF_IN_BOUNDS, playerId) shouldBe
            null
    }

    @Test
    fun `Should not update In Bounds achievement for a team`() {
        val (p1, p2) = preparePlayers(2)
        val panel = makeGolfGamePanel(listOf(p1, p2), true, "18")

        panel.addCompletedRound(drtOuterOne())
        panel.addCompletedRound(drtInnerTwo())
        panel.addCompletedRound(drtOuterThree(), drtOuterThree())
        panel.addCompletedRound(drtOuterFour())
        panel.addCompletedRound(drtOuterTwenty(), drtTrebleFive())
        panel.addCompletedRound(drtInnerSix())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtInnerNine())
        panel.addCompletedRound(drtOuterTen())
        panel.addCompletedRound(drtOuterEleven())
        panel.addCompletedRound(drtTrebleTwelve())
        panel.addCompletedRound(drtMissThirteen(), drtMissThirteen(), drtOuterThirteen())
        panel.addCompletedRound(drtOuterFourteen())
        panel.addCompletedRound(drtInnerFifteen())
        panel.addCompletedRound(drtMissSixteen(), drtDoubleSixteen())
        panel.addCompletedRound(drtOuterSeventeen())
        panel.addCompletedRound(drtOuterEighteen())

        panel.gameEntity.isFinished() shouldBe true
        AchievementEntity.retrieveAchievement(AchievementType.GOLF_IN_BOUNDS, p1.rowId) shouldBe
            null
        AchievementEntity.retrieveAchievement(AchievementType.GOLF_IN_BOUNDS, p2.rowId) shouldBe
            null
    }

    @Test
    fun `Should not add to In Bounds achievement for a 9-hole game`() {
        val playerId = randomGuid()
        val panel = makeGolfGamePanel(playerId, gameParams = "9")

        panel.addCompletedRound(drtOuterOne())
        panel.addCompletedRound(drtInnerTwo())
        panel.addCompletedRound(drtOuterThree(), drtOuterThree())
        panel.addCompletedRound(drtOuterFour())
        panel.addCompletedRound(drtOuterTwenty(), drtTrebleFive())
        panel.addCompletedRound(drtInnerSix())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtInnerNine())

        panel.gameEntity.isFinished() shouldBe true
        AchievementEntity.retrieveAchievement(AchievementType.GOLF_IN_BOUNDS, playerId) shouldBe
            null
    }

    @Test
    fun `Should unlock individual win achievement`() {
        val players = preparePlayers(2)
        val panel = makeGolfGamePanel(players, false, "9")

        panel.addCompletedRound(drtOuterOne())
        panel.addCompletedRound(drtDoubleOne())
        panel.addCompletedRound(drtInnerTwo())
        panel.addCompletedRound(drtDoubleTwo())
        panel.addCompletedRound(drtOuterThree(), drtOuterThree())
        panel.addCompletedRound(drtDoubleThree())
        panel.addCompletedRound(drtOuterFour())
        panel.addCompletedRound(drtDoubleFour())
        panel.addCompletedRound(drtOuterTwenty(), drtTrebleFive())
        panel.addCompletedRound(drtDoubleFive())
        panel.addCompletedRound(drtInnerSix())
        panel.addCompletedRound(drtDoubleSix())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtInnerNine())
        panel.addCompletedRound(drtDoubleNine())
        panel.gameEntity.isFinished() shouldBe true

        val (p1, p2) = players
        AchievementEntity.retrieveAchievement(AchievementType.GOLF_GAMES_WON, p1.rowId) shouldBe
            null

        val a = AchievementEntity.retrieveAchievement(AchievementType.GOLF_GAMES_WON, p2.rowId)!!
        a.gameIdEarned shouldBe panel.gameEntity.rowId
        a.achievementDetail shouldBe "9"
    }

    @Test
    fun `Should unlock team win achievement`() {
        val players = preparePlayers(4)
        val panel = makeGolfGamePanel(players, true, "9")

        panel.addCompletedRound(drtOuterOne())
        panel.addCompletedRound(drtDoubleOne())
        panel.addCompletedRound(drtInnerTwo())
        panel.addCompletedRound(drtDoubleTwo())
        panel.addCompletedRound(drtOuterThree(), drtOuterThree())
        panel.addCompletedRound(drtDoubleThree())
        panel.addCompletedRound(drtOuterFour())
        panel.addCompletedRound(drtDoubleFour())
        panel.addCompletedRound(drtOuterTwenty(), drtTrebleFive())
        panel.addCompletedRound(drtDoubleFive())
        panel.addCompletedRound(drtInnerSix())
        panel.addCompletedRound(drtDoubleSix())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleSeven())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtDoubleEight())
        panel.addCompletedRound(drtInnerNine())
        panel.addCompletedRound(drtDoubleNine())
        panel.gameEntity.isFinished() shouldBe true

        val (p1, p2, p3, p4) = players
        AchievementEntity.retrieveAchievement(
            AchievementType.GOLF_TEAM_GAMES_WON,
            p1.rowId
        ) shouldBe null

        AchievementEntity.retrieveAchievement(
            AchievementType.GOLF_TEAM_GAMES_WON,
            p2.rowId
        ) shouldBe null

        val a =
            AchievementEntity.retrieveAchievement(AchievementType.GOLF_TEAM_GAMES_WON, p3.rowId)!!
        a.gameIdEarned shouldBe panel.gameEntity.rowId
        a.achievementDetail shouldBe "9"

        val a2 =
            AchievementEntity.retrieveAchievement(AchievementType.GOLF_TEAM_GAMES_WON, p4.rowId)!!
        a2.gameIdEarned shouldBe panel.gameEntity.rowId
        a2.achievementDetail shouldBe "9"
    }
}
