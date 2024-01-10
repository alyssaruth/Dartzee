package dartzee.stats

import dartzee.helper.AbstractTest
import dartzee.helper.GAME_WRAPPER_301_1
import dartzee.helper.GAME_WRAPPER_301_2
import dartzee.helper.makeDart
import dartzee.helper.makeGameWrapper
import dartzee.helper.makeX01Rounds
import dartzee.`object`.Dart
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TestGameWrapperX01Utils : AbstractTest() {
    @Test
    fun `Should report three dart average correctly`() {
        val gameWrapper = makeGameWrapper()
        gameWrapper.getThreeDartAverage(70) shouldBe -1.0

        val d1 = makeDart(20, 1, startingScore = 100)
        val d2 = makeDart(20, 2, startingScore = 100)
        val d3 = makeDart(10, 0, startingScore = 80)
        val d4 = makeDart(5, 3, startingScore = 100)

        listOf(d1, d2, d3, d4).forEach(gameWrapper::addDart)
        val result = gameWrapper.getThreeDartAverage(70)
        result shouldBe 56.25
    }

    @Test
    fun `Should return scoring darts correctly based on threshold`() {
        val round1 = listOf(Dart(20, 3), Dart(20, 1), Dart(5, 1)) // 115
        val round2 = listOf(Dart(20, 1), Dart(20, 1), Dart(1, 1)) //  74
        val round3 = listOf(Dart(9, 1), Dart(14, 1), Dart(1, 1)) //  50
        val rounds = makeX01Rounds(200, round1, round2, round3)

        val wrapper = makeGameWrapper()
        rounds.flatten().forEach(wrapper::addDart)

        wrapper.getScoringDarts(118).shouldContainExactly(round1)
        wrapper.getScoringDarts(100).shouldContainExactly(round1 + round2.first())
    }

    @Test
    fun `Should populate the three dart score map correctly`() {
        // Full map with the lowest threshold possible
        val map = mutableMapOf<Int, ThreeDartScoreWrapper>()
        GAME_WRAPPER_301_1.populateThreeDartScoreMap(map, 62)
        GAME_WRAPPER_301_2.populateThreeDartScoreMap(map, 62)

        map.keys.shouldContainExactlyInAnyOrder(120, 60, 58, 57, 45, 1)

        val fortyFive = map.getValue(45)
        fortyFive.createRows().shouldContainExactly(arrayOf("20, 20, 5", 2, 1L))

        val sixty = map.getValue(60)
        sixty
            .createRows()
            .shouldContainExactly(arrayOf("20, 20, 20", 1, 1L), arrayOf("T12, 20, 4", 1, 2L))

        // Higher threshold - test rounds are knocked out
        val shorterMap = mutableMapOf<Int, ThreeDartScoreWrapper>()
        GAME_WRAPPER_301_1.populateThreeDartScoreMap(shorterMap, 140)
        GAME_WRAPPER_301_2.populateThreeDartScoreMap(shorterMap, 140)

        shorterMap.keys.shouldContainExactlyInAnyOrder(120, 60, 57, 45)
    }
}
