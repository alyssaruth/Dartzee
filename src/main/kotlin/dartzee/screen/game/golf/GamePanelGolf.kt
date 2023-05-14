package dartzee.screen.game.golf

import dartzee.achievements.AchievementType
import dartzee.achievements.retrieveAchievementForDetail
import dartzee.ai.DartsAiModel
import dartzee.core.util.doGolfMiss
import dartzee.db.AchievementEntity
import dartzee.db.GameEntity
import dartzee.game.state.GolfPlayerState
import dartzee.game.state.IWrappedParticipant
import dartzee.`object`.ComputedPoint
import dartzee.screen.GameplayDartboard
import dartzee.screen.game.AbstractDartsGameScreen
import dartzee.screen.game.GamePanelFixedLength
import dartzee.screen.game.scorer.DartsScorerGolf

class GamePanelGolf(parent: AbstractDartsGameScreen, game: GameEntity, totalPlayers: Int) :
        GamePanelFixedLength<DartsScorerGolf, GameplayDartboard, GolfPlayerState>(parent, game, totalPlayers)
{
    //Number of rounds - 9 holes or 18?
    override val totalRounds = Integer.parseInt(game.gameParams)

    override fun factoryDartboard() = GameplayDartboard()
    override fun factoryState(pt: IWrappedParticipant) = GolfPlayerState(pt)

    private fun getScoreForMostRecentDart() : Int
    {
        val lastDart = getDartsThrown().last()

        val targetHole = currentRoundNumber
        return lastDart.getGolfScore(targetHole)
    }

    override fun computeAiDart(model: DartsAiModel): ComputedPoint
    {
        val targetHole = currentRoundNumber
        val dartNo = dartsThrownCount() + 1
        return model.throwGolfDart(targetHole, dartNo)
    }

    override fun shouldStopAfterDartThrown(): Boolean
    {
        val dartsThrownCount = dartsThrownCount()
        if (dartsThrownCount == 3)
        {
            return true
        }

        val score = getScoreForMostRecentDart()
        if (getCurrentPlayerState().isHuman())
        {
            return score == 1
        }
        else
        {
            val model = getCurrentPlayerStrategy()
            val stopThreshold = model.getStopThresholdForDartNo(dartsThrownCount)

            return score <= stopThreshold
        }
    }

    override fun saveDartsAndProceed()
    {
        unlockAchievements()
        commitRound()

        finishRound()
    }

    private fun unlockAchievements()
    {
        val size = getDartsThrown().size
        val dartsRisked = getDartsThrown().subList(0, size - 1)
        val pointsRisked = dartsRisked.sumOf { 5 - it.getGolfScore(currentRoundNumber) }

        if (pointsRisked > 0)
        {
            AchievementEntity.insertAchievementWithCounter(AchievementType.GOLF_POINTS_RISKED, getCurrentPlayerId(), gameEntity.rowId, "$currentRoundNumber", pointsRisked)
        }

        if (getDartsThrown().last().getGolfScore(currentRoundNumber) == 1
         && retrieveAchievementForDetail(AchievementType.GOLF_COURSE_MASTER, getCurrentPlayerId(), "$currentRoundNumber") == null)
        {
            AchievementEntity.insertAchievement(AchievementType.GOLF_COURSE_MASTER, getCurrentPlayerId(), getGameId(), "$currentRoundNumber")
        }
    }

    override fun factoryScorer(participant: IWrappedParticipant) = DartsScorerGolf(participant)

    override fun shouldAIStop() = false

    override fun doMissAnimation()
    {
        dartboard.doGolfMiss()
    }

    override fun factoryStatsPanel(gameParams: String) = GameStatisticsPanelGolf()
}
