package dartzee.screen.game.scorer

import dartzee.achievements.x01.AchievementX01BestFinish
import dartzee.achievements.x01.AchievementX01BestGame
import dartzee.achievements.x01.AchievementX01GamesWon
import dartzee.game.state.IWrappedParticipant
import dartzee.game.state.TestPlayerState
import dartzee.getRows
import dartzee.helper.AbstractTest
import dartzee.helper.insertParticipant
import dartzee.helper.preparePlayers
import dartzee.`object`.Dart
import dartzee.screen.game.makeSingleParticipant
import dartzee.screen.game.makeTeam
import dartzee.shouldHaveColours
import dartzee.utils.DartsColour
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test

class TestAbstractDartsScorer: AbstractTest()
{
    @Test
    fun `Should clear the data model on stateChanged, and call child implementation`()
    {
        val roundOne = listOf(Dart(1, 1), Dart(1, 1), Dart(2, 1))
        val roundTwo = listOf(Dart(2, 3), Dart(2, 2), Dart(2, 3))
        val state = TestPlayerState(insertParticipant(), completedRounds = mutableListOf(roundOne, roundTwo))

        val scorer = TestDartsScorer()
        scorer.init()
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
        scorer.init()
        scorer.stateChanged(state)

        scorer.lblResult.shouldHaveColours(DartsColour.THIRD_COLOURS)
        scorer.lblResult.text shouldBe "30"
    }

    @Test
    fun `Should correctly update bold text when selected and deselected`()
    {
        val scorer = TestDartsScorer()
        scorer.init()

        val state = TestPlayerState(insertParticipant(), isActive = false)
        scorer.stateChanged(state)
        scorer.lblName.text shouldBe "<html>Clive</html>"

        state.updateActive(true)
        scorer.stateChanged(state)
        scorer.lblName.text shouldBe "<html><b>Clive</b></html>"

        scorer.gameFinished()
        scorer.lblName.text shouldBe "<html>Clive</html>"
    }

    @Test
    fun `Should not set score if it is unset`()
    {
        val state = TestPlayerState(insertParticipant(finishingPosition = -1), scoreSoFar = -1)

        val scorer = TestDartsScorer(state.wrappedParticipant)
        scorer.init()

        val startingColours = Pair(scorer.lblResult.background, scorer.lblResult.foreground)
        scorer.stateChanged(state)
        scorer.lblResult.shouldHaveColours(startingColours)
        scorer.lblResult.text shouldBe ""
    }
    
    @Test
    fun `Should layer achievements, and show them in sequence as they are closed`()
    {
        val scorer = TestDartsScorer()
        scorer.init()

        val achievementOne = AchievementX01BestGame().also { it.attainedValue = 30 }
        val achievementTwo = AchievementX01BestFinish().also { it.attainedValue = 97 }
        val achievementThree = AchievementX01GamesWon().also { it.attainedValue = 1 }

        scorer.achievementUnlocked(achievementOne, "")
        scorer.achievementUnlocked(achievementTwo, "")
        scorer.achievementUnlocked(achievementThree, "")

        val visibleAchievement = scorer.getAchievementOverlay()
        visibleAchievement.getAchievementName() shouldBe achievementThree.name
        visibleAchievement.close()

        val secondAchievement = scorer.getAchievementOverlay()
        secondAchievement.getAchievementName() shouldBe achievementTwo.name
        secondAchievement.close()

        val thirdAchievement = scorer.getAchievementOverlay()
        thirdAchievement.getAchievementName() shouldBe achievementOne.name
        thirdAchievement.close()

        scorer.findAchievementOverlay() shouldBe null
    }

    @Test
    fun `Should pass player name when achievement unlocked for a team`()
    {
        val (alice, bob) = preparePlayers(2)
        val team = makeTeam(alice, bob)

        val scorer = TestDartsScorer(team)
        scorer.init()

        val achievementOne = AchievementX01BestGame().also { it.attainedValue = 30 }
        val achievementTwo = AchievementX01BestFinish().also { it.attainedValue = 97 }

        scorer.achievementUnlocked(achievementOne, alice.rowId)
        scorer.getAchievementOverlay().getPlayerName() shouldBe "Alice"

        scorer.achievementUnlocked(achievementTwo, bob.rowId)
        scorer.getAchievementOverlay().getPlayerName() shouldBe "Bob"
    }

    private class TestDartsScorer(participant: IWrappedParticipant = makeSingleParticipant()) : AbstractDartsScorer<TestPlayerState>(participant)
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