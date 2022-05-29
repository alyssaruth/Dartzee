package dartzee.screen.game.scorer

import dartzee.game.state.GolfPlayerState
import dartzee.game.state.IWrappedParticipant

class DartsScorerGolf(participant: IWrappedParticipant) :
    AbstractDartsScorer<GolfPlayerState>(participant), IGolfScorerTable
{
    override val fudgeFactor = 0

    override fun getNumberOfColumns() = 5

    override fun initImpl()
    {
        for (i in 0..GOLF_SCORE_COLUMN)
        {
            tableScores.setRenderer(i, GolfDartRenderer(false))
        }
    }

    override fun stateChangedImpl(state: GolfPlayerState)
    {
        setScoreAndFinishingPosition(state)

        populateTable(state.completedRounds)

        if (state.currentRound.isNotEmpty())
        {
            addDartRound(state.currentRound)
        }
    }
}
