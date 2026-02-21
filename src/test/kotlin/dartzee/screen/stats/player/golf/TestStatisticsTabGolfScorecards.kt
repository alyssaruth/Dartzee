package dartzee.screen.stats.player.golf

import com.github.alyssaburlton.swingtest.findChild
import com.github.alyssaburlton.swingtest.getChild
import dartzee.bean.ScrollTableDartsGame
import dartzee.core.bean.ComboBoxItem
import dartzee.core.bean.items
import dartzee.getFirstRow
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.golfFrontNine22
import dartzee.helper.golfFrontNine22EvenRounds
import dartzee.helper.golfFrontNine29
import dartzee.helper.golfFull28_29
import dartzee.helper.golfFull31_22
import dartzee.stats.GameWrapper
import dartzee.stats.GolfMode
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import javax.swing.JComboBox
import org.junit.jupiter.api.Test

class TestStatisticsTabGolfScorecards : AbstractTest() {
    @Test
    fun `Should cope with 0 games`() {
        val tab = StatisticsTabGolfScorecards()
        tab.populateStats()

        val comboBox = tab.comboBoxMode()
        comboBox.items().shouldContainExactly(ComboBoxItem(GolfMode.FULL_18, "N/A"))
    }

    @Test
    fun `Should ignore team games`() {
        val tab = StatisticsTabGolfScorecards()
        tab.setFilteredGames(listOf(golfFrontNine22EvenRounds()), emptyList())
        tab.populateStats()

        val comboBox = tab.comboBoxMode()
        comboBox.items().shouldContainExactly(ComboBoxItem(GolfMode.FULL_18, "N/A"))
    }

    @Test
    fun `Should populate combo box based on game types that are available`() {
        val tab = StatisticsTabGolfScorecards()
        tab.setFilteredGames(listOf(golfFrontNine22()), emptyList())
        tab.populateStats()

        val comboBox = tab.comboBoxMode()
        comboBox.items().shouldContainExactly(ComboBoxItem(GolfMode.FRONT_9, "Front 9"))

        tab.setFilteredGames(listOf(golfFull31_22()), emptyList())
        tab.populateStats()

        comboBox
            .items()
            .shouldContainExactly(
                ComboBoxItem(GolfMode.FRONT_9, "Front 9"),
                ComboBoxItem(GolfMode.BACK_9, "Back 9"),
                ComboBoxItem(GolfMode.FULL_18, "Full 18"),
            )
    }

    @Test
    fun `Should show appropriate screen state for individual stats`() {
        val game = golfFrontNine22()
        val tab = StatisticsTabGolfScorecards()
        tab.setFilteredGames(listOf(game), emptyList())
        tab.populateStats()

        tab.scorecardsOther().shouldBeNull()
        tab.scorecardsMine().getFirstRow().shouldContainExactly(1L, 22)
    }

    @Test
    fun `Should show appropriate screen state when a comparison is included`() {
        val myGame = golfFrontNine29()
        val otherGame = golfFrontNine22()
        val mine = listOf(myGame)
        val other = listOf(otherGame)

        val tab = StatisticsTabGolfScorecards()
        tab.setFilteredGames(mine, other)
        tab.populateStats()

        tab.scorecardsMine().getFirstRow().shouldContainExactly(1L, 29)
        tab.scorecardsOther()!!.getFirstRow().shouldContainExactly(1L, 22)

        tab.scorecardShouldMatch(0, GolfMode.FRONT_9, myGame, "scorecardMine")
        tab.scorecardShouldMatch(0, GolfMode.FRONT_9, otherGame, "scorecardOther")
    }

    @Test
    fun `Mode combo box should work as expected`() {
        val gameOne = golfFrontNine22(1L)
        val gameThree = golfFull31_22(3L)
        val games = listOf(gameOne, golfFrontNine29(2L), gameThree, golfFull28_29(4L))

        val tab = StatisticsTabGolfScorecards()
        tab.setFilteredGames(games, emptyList())
        tab.populateStats()

        tab.comboBoxMode().selectedIndex = 0
        tab.scorecardsMine()
            .getRows()
            .shouldContainExactly(
                listOf<Any>(1L, 22),
                listOf<Any>(2L, 29),
                listOf<Any>(3L, 31),
                listOf<Any>(4L, 28),
            )
        tab.scorecardShouldMatch(0, GolfMode.FRONT_9, gameOne)

        tab.comboBoxMode().selectedIndex = 1 // Back 9
        tab.scorecardsMine()
            .getRows()
            .shouldContainExactly(listOf<Any>(3L, 22), listOf<Any>(4L, 29))
        tab.scorecardShouldMatch(9, GolfMode.BACK_9, gameThree)

        tab.comboBoxMode().selectedIndex = 2 // Full 18
        tab.scorecardsMine()
            .getRows()
            .shouldContainExactly(listOf<Any>(3L, 53), listOf<Any>(4L, 57))
        tab.scorecardShouldMatch(0, GolfMode.FULL_18, gameThree)
    }

    @Test
    fun `Selecting a row should populate the scorecard`() {
        val gameOne = golfFull31_22(1L)
        val gameTwo = golfFull28_29(2L)
        val games = listOf(gameOne, gameTwo)
        val tab = StatisticsTabGolfScorecards()
        tab.setFilteredGames(games, emptyList())
        tab.populateStats()
        tab.comboBoxMode().selectedIndex = 2 // Full 18

        tab.scorecardsMine()
            .getRows()
            .shouldContainExactly(listOf<Any>(1L, 53), listOf<Any>(2L, 57))
        tab.scorecardShouldMatch(0, GolfMode.FULL_18, gameOne)

        tab.scorecardsMine().selectRow(1)
        tab.scorecardShouldMatch(0, GolfMode.FULL_18, gameTwo)
    }

    private fun StatisticsTabGolfScorecards.comboBoxMode() =
        getChild<JComboBox<ComboBoxItem<GolfMode>>>()

    private fun StatisticsTabGolfScorecards.scorecardsMine() =
        getChild<ScrollTableDartsGame> { it.testId == "ScorecardsMine" }

    private fun StatisticsTabGolfScorecards.scorecardsOther() =
        findChild<ScrollTableDartsGame> { it.testId == "ScorecardsOther" }

    private fun StatisticsTabGolfScorecards.displayedScorecard(testId: String) =
        getChild<GolfStatsScorecard> { it.testId == testId }

    private fun StatisticsTabGolfScorecards.scorecardShouldMatch(
        fudgeFactor: Int,
        golfMode: GolfMode,
        game: GameWrapper,
        testId: String = "scorecardMine",
    ) {
        val displayed = displayedScorecard(testId)
        val expected =
            GolfStatsScorecard(fudgeFactor, false).also {
                it.populateTable(game.getGolfRounds(golfMode))
            }
        displayed.tableScores.getRows() shouldBe expected.tableScores.getRows()
    }
}
