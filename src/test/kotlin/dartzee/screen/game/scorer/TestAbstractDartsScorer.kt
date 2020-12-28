package dartzee.screen.game.scorer

import dartzee.`object`.Dart
import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.achievements.x01.AchievementX01GamesWon
import dartzee.game.state.TestPlayerState
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.insertPlayer
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import java.awt.Color

class TestAbstractDartsScorer: AbstractTest()
{
    @Test
    fun `Should clear the data model on stateChanged, and call child implementation`()
    {
        val roundOne = listOf(Dart(1, 1), Dart(1, 1), Dart(2, 1))
        val roundTwo = listOf(Dart(2, 3), Dart(2, 2), Dart(2, 3))
        val state = TestPlayerState(insertParticipant(), completedRounds = mutableListOf(roundOne, roundTwo))

        val scorer = TestDartsScorer()
        scorer.init(null)
        scorer.addRow(arrayOfNulls<Any?>(2))

        scorer.stateChanged(state)

        scorer.tableScores.getRows().shouldContainExactly(
                roundOne + listOf(null, null),
                roundTwo + listOf(null, null)
        )
    }

    @Test
    fun `Should set score and finishing position if it is set`()
    {
        val state = TestPlayerState(insertParticipant(finishingPosition = 3), scoreSoFar = 30)

        val scorer = TestDartsScorer()
        scorer.init(null)
        scorer.stateChanged(state)

        scorer.lblResult.shouldHaveColours(DartsColour.THIRD_COLOURS)
        scorer.lblResult.text shouldBe "30"
    }

    @Test
    fun `Should correctly update colours when selected and deselected`()
    {
        val scorer = TestDartsScorer()
        scorer.init(insertPlayer())

        val state = TestPlayerState(insertParticipant(), isActive = false)
        scorer.stateChanged(state)
        scorer.lblName.foreground shouldBe Color.BLACK

        state.updateActive(true)
        scorer.stateChanged(state)
        scorer.lblName.foreground shouldBe Color.RED
    }

    @Test
    fun `Should not set score if it is unset`()
    {
        val state = TestPlayerState(insertParticipant(finishingPosition = -1), scoreSoFar = -1)

        val scorer = TestDartsScorer()
        scorer.init(null)

        val startingColours = Pair(scorer.lblResult.background, scorer.lblResult.foreground)
        scorer.stateChanged(state)
        scorer.lblResult.shouldHaveColours(startingColours)
        scorer.lblResult.text shouldBe ""
    }
    
    @Test
    fun `Should layer achievements, and show them in sequence as they are closed`()
    {
        val scorer = TestDartsScorer()
        scorer.init(null)

        val achievementOne = AchievementX01BestGame().also { it.attainedValue = 30 }
        val achievementTwo = AchievementX01BestFinish().also { it.attainedValue = 97 }
        val achievementThree = AchievementX01GamesWon().also { it.attainedValue = 1 }

        scorer.achievementUnlocked(achievementOne)
        scorer.achievementUnlocked(achievementTwo)
        scorer.achievementUnlocked(achievementThree)

        val visibleAchievement = scorer.getAchievementOverlay()!!
        visibleAchievement.getAchievementName() shouldBe achievementThree.name
        visibleAchievement.close()

        val secondAchievement = scorer.getAchievementOverlay()!!
        secondAchievement.getAchievementName() shouldBe achievementTwo.name
        secondAchievement.close()

        val thirdAchievement = scorer.getAchievementOverlay()!!
        thirdAchievement.getAchievementName() shouldBe achievementOne.name
        thirdAchievement.close()

        scorer.getAchievementOverlay() shouldBe null
    }

    private class TestDartsScorer: AbstractDartsScorer<TestPlayerState>()
    {
        override fun getNumberOfColumns() = 5
        override fun getNumberOfColumnsForAddingNewDart() = 3

        override fun initImpl()
        {

        }

        override fun stateChangedImpl(state: TestPlayerState)
        {
            setScoreAndFinishingPosition(state)
            state.completedRounds.forEach(::addDartRound)
        }
    }
}