package dartzee.screen.stats.player.golf

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.golfAllMisses
import dartzee.helper.golfFrontNine22
import dartzee.helper.golfFrontNine22EvenRounds
import dartzee.helper.golfFrontNine29
import dartzee.helper.golfFull28_29
import dartzee.helper.golfFull31_22
import dartzee.helper.golfFullOptimal
import dartzee.stats.GameWrapper
import dartzee.stats.GolfMode
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class TestStatisticsTabGolfOptimalScorecard : AbstractTest()
{
    @Test
    fun `Should cope with 0 games`()
    {
        val tab = StatisticsTabGolfOptimalScorecard()
        tab.populateStats()
        tab.scorecardShouldMatch(golfAllMisses(), emptyList())
    }

    @Test
    fun `Should show appropriate screen state for individual stats`()
    {
        val game = golfFull28_29()
        val tab = StatisticsTabGolfOptimalScorecard()
        tab.setFilteredGames(listOf(game), emptyList())
        tab.populateStats()

        tab.scorecardOther().shouldBeNull()
        tab.scorecardShouldMatch(game, (1..18).map { 1L })
    }

    @Test
    fun `Should show appropriate screen state when a comparison is included`()
    {
        val myGame = golfFull28_29(1L)
        val otherGame = golfFull31_22(2L)
        val mine = listOf(myGame)
        val other = listOf(otherGame)

        val tab = StatisticsTabGolfOptimalScorecard()
        tab.setFilteredGames(mine, other)
        tab.populateStats()

        tab.scorecardOther().shouldNotBeNull()

        tab.scorecardShouldMatch(myGame, (1..18).map { 1L }, "scorecardMine")
        tab.scorecardShouldMatch(otherGame, (1..18).map { 2L }, "scorecardOther")
    }

    @Test
    fun `Should optimise across games`()
    {
        val gameOne = golfFrontNine22(1L, Timestamp(500))
        val gameTwo = golfFrontNine29(2L, Timestamp(1000))
        val gameThree = golfFull31_22(3L, Timestamp(1500))
        val gameFour = golfFull28_29(4L, Timestamp(2000))

        val tab = StatisticsTabGolfOptimalScorecard()
        tab.setFilteredGames(listOf(gameOne, gameTwo, gameThree, gameFour), emptyList())
        tab.populateStats()

        val (optimalGame, optimalGameIds) = golfFullOptimal()
        tab.scorecardShouldMatch(optimalGame, optimalGameIds)
    }

    @Test
    fun `Should cope with team games that have partial rounds`()
    {
        val game = golfFrontNine22EvenRounds()

        val tab = StatisticsTabGolfOptimalScorecard()
        tab.setFilteredGames(listOf(game), emptyList())
        tab.populateStats()

        val scorecardRows = tab.scorecardMine().tableScores.getRows()
        scorecardRows[0][4] shouldBe 5 // score
        scorecardRows[0][5] shouldBe null // game

        scorecardRows[3][4] shouldBe 4 // score
        scorecardRows[3][5] shouldBe 1 // game
    }

    private fun StatisticsTabGolfOptimalScorecard.scorecardMine() = getChild<GolfStatsScorecard> { it.testId == "scorecardMine" }
    private fun StatisticsTabGolfOptimalScorecard.scorecardOther() = findChild<GolfStatsScorecard> { it.testId == "scorecardOther" }
    private fun StatisticsTabGolfOptimalScorecard.scorecardShouldMatch(game: GameWrapper, gameIds: List<Long>, testId: String = "scorecardMine") {
        val expected = GolfStatsScorecard(0, true).also { it.populateTable(game.getGolfRounds(GolfMode.FULL_18)) }
        expected.addGameIds(gameIds)

        val scorecard = getChild<GolfStatsScorecard> { it.testId == testId }
        scorecard.tableScores.getRows() shouldBe expected.tableScores.getRows()
    }
}